package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.ui.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectDetailScreen(
    uiState: UiState<Project>,
    showBackButton: Boolean = true,
    onBack: () -> Unit,
    onNewTaskClick: (NewTask) -> Unit,
    onDeleteClick: (ConfirmDelete) -> Unit,
    onTaskToggle: (Int) -> Unit,
    onMarkAllDone: () -> Unit,
    highlightTaskId: Int? = null,
) {
    when (uiState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.message, color = MaterialTheme.colorScheme.error)
        }

        is UiState.Success -> {
            val project = uiState.data
            val lazyListState = rememberLazyListState()
            var toolbarExpanded by rememberSaveable { mutableStateOf(true) }
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

            LaunchedEffect(highlightTaskId) {
                if (highlightTaskId != null) lazyListState.animateScrollToItem(1)
            }

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeFlexibleTopAppBar(
                        title = { Text(project.name) },
                        subtitle = { Text("${project.tasks.size} tarefas") },
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            if (showBackButton) {
                                IconButton(onClick = dropUnlessResumed(block = onBack)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                }
                            }
                        },
                    )
                },
                floatingActionButton = {
                    HorizontalFloatingToolbar(
                        expanded = toolbarExpanded,
                        floatingActionButton = {
                            FloatingToolbarDefaults.VibrantFloatingActionButton(
                                onClick = dropUnlessResumed {
                                    onNewTaskClick(NewTask(projectId = project.id))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
                            }
                        },
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    ) {
                        IconButton(
                            onClick = { onMarkAllDone() },
                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                        ) { Icon(Icons.Default.Done, contentDescription = "Marcar todas concluídas") }

                        IconButton(
                            onClick = dropUnlessResumed {
                                onDeleteClick(ConfirmDelete(project.id, project.name))
                            },
                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                        ) { Icon(Icons.Default.Delete, contentDescription = "Excluir projeto") }
                    }
                },
            ) { padding ->
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .floatingToolbarVerticalNestedScroll(
                            expanded = toolbarExpanded,
                            onExpand = { toolbarExpanded = true },
                            onCollapse = { toolbarExpanded = false },
                        ),
                ) {
                    if (project.tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhuma tarefa ainda.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            val colors =
                                ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .selectableGroup(),
                                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                            ) {
                                project.tasks.forEachIndexed { index, task ->
                                    SegmentedListItem(
                                        selected = task.done || task.id == highlightTaskId,
                                        onClick = { onTaskToggle(task.id) },
                                        shapes = ListItemDefaults.segmentedShapes(index, project.tasks.size),
                                        colors = colors,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text(task.title) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

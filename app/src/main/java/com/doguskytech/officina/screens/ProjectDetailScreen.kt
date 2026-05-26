package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onBack: () -> Unit,
    onNewTaskClick: (NewTask) -> Unit,
    onDeleteClick: (ConfirmDelete) -> Unit,
    onTaskToggle: (Int) -> Unit,
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
            LaunchedEffect(highlightTaskId) {
                if (highlightTaskId != null) lazyListState.animateScrollToItem(1)
            }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(project.name) },
                        subtitle = { Text("${project.tasks.size} tarefas") },
                        navigationIcon = {
                            IconButton(onClick = dropUnlessResumed(block = onBack)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                            }
                        },
                    )
                }
            ) { padding ->
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${project.tasks.size} tarefas", style = MaterialTheme.typography.labelLarge)
                            Button(onClick = dropUnlessResumed {
                                onNewTaskClick(NewTask(projectId = project.id))
                            }) {
                                Text("+ Nova Tarefa")
                            }
                        }
                        HorizontalDivider()
                    }

                    if (project.tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
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
                            // SegmentedListItem: visual de grupo onde o primeiro item tem cantos
                            // arredondados no topo, o último na base, e os do meio são retos.
                            // selected = task.done: itens concluídos ficam destacados.
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

                    item {
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            TextButton(onClick = dropUnlessResumed {
                                onDeleteClick(ConfirmDelete(project.id, project.name))
                            }) {
                                Text("Excluir projeto", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.doguskytech.officina.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.R
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.TaskStatus
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.itemEnterTransition
import com.doguskytech.officina.ui.itemExitTransition
import com.doguskytech.officina.ui.labelRes
import com.doguskytech.officina.ui.priorityColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectDetailScreen(
    uiState: UiState<Project>,
    showBackButton: Boolean = true,
    onBack: () -> Unit,
    onNewTaskClick: (NewTask) -> Unit,
    onDeleteClick: (ConfirmDelete) -> Unit,
    onMarkAllDone: () -> Unit,
    onCompleteTasks: (Set<Int>) -> Unit,
    onDeleteTasks: (Set<Int>) -> Unit,
    onCancelTasks: (Set<Int>) -> Unit = {},
    onStartProject: () -> Unit = {},
    onFinishProject: () -> Unit = {},
    onCancelProject: () -> Unit = {},
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
            var selectedTaskIds by remember { mutableStateOf(emptySet<Int>()) }
            val isInSelectionMode = selectedTaskIds.isNotEmpty()
            var showMarkAllConfirm by remember { mutableStateOf(false) }
            var showProjectMenu by remember { mutableStateOf(false) }

            BackHandler(enabled = isInSelectionMode) { selectedTaskIds = emptySet() }

            LaunchedEffect(highlightTaskId) {
                if (highlightTaskId != null) lazyListState.animateScrollToItem(1)
            }

            if (showMarkAllConfirm) {
                val pending = project.tasks.count { it.isPending }
                AlertDialog(
                    onDismissRequest = { showMarkAllConfirm = false },
                    title = { Text(stringResource(R.string.confirm_complete_all_title)) },
                    text = { Text(pluralStringResource(R.plurals.pending_tasks_confirm_body, pending, pending)) },
                    confirmButton = {
                        TextButton(onClick = { onMarkAllDone(); showMarkAllConfirm = false }) {
                            Text(stringResource(R.string.action_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMarkAllConfirm = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    },
                )
            }

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeFlexibleTopAppBar(
                        title = { Text(project.name) },
                        subtitle = {
                            if (isInSelectionMode) {
                                Text(pluralStringResource(R.plurals.selected_tasks_count, selectedTaskIds.size, selectedTaskIds.size))
                            } else {
                                val taskCount = pluralStringResource(R.plurals.tasks_count, project.tasks.size, project.tasks.size)
                                Text("$taskCount · ${stringResource(project.status.labelRes)}")
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            if (showBackButton) {
                                IconButton(onClick = dropUnlessResumed(block = onBack)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                                }
                            }
                        },
                        actions = {
                            if (!isInSelectionMode && project.isActive) {
                                Box {
                                    IconButton(onClick = { showProjectMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = showProjectMenu,
                                        onDismissRequest = { showProjectMenu = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_cancel_project)) },
                                            onClick = { showProjectMenu = false; onCancelProject() },
                                            leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) },
                                        )
                                    }
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
                                    if (project.isActive) {
                                        selectedTaskIds = emptySet()
                                        onNewTaskClick(NewTask(projectId = project.id))
                                    }
                                },
                                modifier = Modifier.alpha(if (project.isActive) 1f else 0.38f),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new_task))
                            }
                        },
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    ) {
                        // null = selection mode; non-null = status-based actions
                        AnimatedContent(
                            targetState = if (isInSelectionMode) null else project.status,
                            transitionSpec = { itemEnterTransition togetherWith itemExitTransition },
                            label = "toolbarActions",
                        ) { status ->
                            Row {
                                if (status == null) {
                                    // Multi-select mode
                                    IconButton(
                                        onClick = {
                                            onCompleteTasks(selectedTaskIds)
                                            selectedTaskIds = emptySet()
                                        },
                                        modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = stringResource(R.string.cd_complete_selected))
                                    }
                                    IconButton(
                                        onClick = {
                                            onCancelTasks(selectedTaskIds)
                                            selectedTaskIds = emptySet()
                                        },
                                        modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                    ) {
                                        Icon(Icons.Filled.Cancel, contentDescription = stringResource(R.string.cd_cancel_selected))
                                    }
                                    IconButton(
                                        onClick = {
                                            onDeleteTasks(selectedTaskIds)
                                            selectedTaskIds = emptySet()
                                        },
                                        modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                    ) {
                                        Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.cd_delete_selected))
                                    }
                                } else when (status) {
                                    ProjectStatus.WAITING -> {
                                        IconButton(
                                            onClick = { onStartProject() },
                                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.action_start_project))
                                        }
                                        IconButton(
                                            onClick = dropUnlessResumed {
                                                onDeleteClick(ConfirmDelete(project.id, project.name))
                                            },
                                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_project))
                                        }
                                    }
                                    ProjectStatus.IN_PROGRESS -> {
                                        IconButton(
                                            onClick = { showMarkAllConfirm = true },
                                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                        ) {
                                            Icon(Icons.Filled.DoneAll, contentDescription = stringResource(R.string.cd_complete_all))
                                        }
                                        IconButton(
                                            onClick = { onFinishProject() },
                                            enabled = project.canFinish,
                                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                        ) {
                                            Icon(Icons.Default.Done, contentDescription = stringResource(R.string.action_finish_project))
                                        }
                                    }
                                    ProjectStatus.DONE, ProjectStatus.CANCELLED -> {
                                        IconButton(
                                            onClick = dropUnlessResumed {
                                                onDeleteClick(ConfirmDelete(project.id, project.name))
                                            },
                                            modifier = Modifier.focusProperties { canFocus = toolbarExpanded },
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_project))
                                        }
                                    }
                                }
                            }
                        }
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
                    // "Ready to finish" banner
                    if (project.canFinish && project.isActive && !isInSelectionMode) {
                        item {
                            AnimatedVisibility(visible = true) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            stringResource(R.string.all_tasks_done_banner),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(onClick = onFinishProject) {
                                            Text(stringResource(R.string.action_finish_now))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (project.tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    stringResource(R.string.no_tasks_yet),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        item {
                            val itemColors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                            ) {
                                project.tasks.forEachIndexed { index, task ->
                                    SegmentedListItem(
                                        selected = task.id in selectedTaskIds,
                                        onClick = {
                                            selectedTaskIds =
                                                if (task.id in selectedTaskIds) selectedTaskIds - task.id
                                                else selectedTaskIds + task.id
                                        },
                                        shapes = ListItemDefaults.segmentedShapes(index, project.tasks.size),
                                        colors = itemColors,
                                        trailingContent = {
                                            AnimatedContent(
                                                targetState = task.status,
                                                transitionSpec = { itemEnterTransition togetherWith itemExitTransition },
                                                label = "taskIcon_${task.id}",
                                            ) { status ->
                                                when (status) {
                                                    TaskStatus.DONE -> Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    TaskStatus.CANCELLED -> Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    TaskStatus.PENDING -> Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(priorityColor(task.priority)),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.Flag,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.surface,
                                                            modifier = Modifier.size(14.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            text = task.title,
                                            textDecoration = if (task.status == TaskStatus.CANCELLED)
                                                TextDecoration.LineThrough else null,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

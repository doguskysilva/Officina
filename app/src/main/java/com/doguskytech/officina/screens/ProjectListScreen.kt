package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.R
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.labelRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectListScreen(
    uiState: UiState<List<Project>>,
    selectedProjectId: Int?,
    onProjectClick: (ProjectDetail) -> Unit,
    onSortClick: () -> Unit,
    onNewProjectClick: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                subtitle = if (uiState is UiState.Success) {
                    { Text(pluralStringResource(R.plurals.projects_count, uiState.data.size, uiState.data.size)) }
                } else null,
                actions = {
                    IconButton(onClick = dropUnlessResumed { onSortClick() }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.cd_sort))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = dropUnlessResumed { onNewProjectClick() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new_project))
            }
        },
    ) { padding ->
        when (uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.message, color = MaterialTheme.colorScheme.error) }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.data, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            selected = project.id == selectedProjectId,
                            onClick = dropUnlessResumed {
                                onProjectClick(ProjectDetail(project.id, project.name))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = project.tasks.size
    val done = project.tasks.count { it.done }
    val doneRatio = if (total == 0) 0f else done.toFloat() / total

    val progressColor = when (project.status) {
        ProjectStatus.WAITING     -> MaterialTheme.colorScheme.outline
        ProjectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        ProjectStatus.DONE        -> MaterialTheme.colorScheme.tertiary
        ProjectStatus.CANCELLED   -> MaterialTheme.colorScheme.outlineVariant
    }

    val cardColors = if (selected) {
        CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.elevatedCardColors()
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = cardColors,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                CircularProgressIndicator(
                    progress = { doneRatio },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 5.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = progressColor,
                )
                Text(
                    text = if (total == 0) "–" else "$done/$total",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StatusBadge(status = project.status)
                }

                val pendingCount = project.pendingCount
                Text(
                    text = when {
                        total == 0 -> stringResource(R.string.no_tasks_yet)
                        pendingCount > 0 -> pluralStringResource(R.plurals.pending_tasks_count, pendingCount, pendingCount)
                        else -> stringResource(R.string.all_tasks_resolved)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ProjectStatus) {
    val (containerColor, contentColor) = when (status) {
        ProjectStatus.WAITING ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        ProjectStatus.IN_PROGRESS ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ProjectStatus.DONE ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ProjectStatus.CANCELLED ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = stringResource(status.labelRes),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

package com.doguskytech.officina.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarWithGapState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.doguskytech.officina.R
import com.doguskytech.officina.data.Priority
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.itemEnterTransition
import com.doguskytech.officina.ui.itemExitTransition
import com.doguskytech.officina.ui.priorityColor
import kotlinx.coroutines.launch

private enum class StatusFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    PENDING(R.string.filter_pending),
    DONE(R.string.filter_done),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskListScreen(
    uiState: UiState<List<TaskWithProject>>,
    onTaskClick: (projectId: Int, projectName: String, taskId: Int) -> Unit,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarWithGapState()
    val scope = rememberCoroutineScope()
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val colors = SearchBarDefaults.appBarWithSearchColors()
    val itemColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    var statusFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var priorityFilter by remember { mutableStateOf<Priority?>(null) }

    val query = textFieldState.text.toString()
    val allTasks = if (uiState is UiState.Success) uiState.data else emptyList()

    val searchResults = remember(query, allTasks) {
        if (query.isBlank()) allTasks
        else allTasks.filter { it.task.title.contains(query, ignoreCase = true) }
    }

    val displayedTasks = remember(allTasks, statusFilter, priorityFilter) {
        allTasks
            .filter {
                when (statusFilter) {
                    StatusFilter.ALL     -> true
                    StatusFilter.PENDING -> !it.task.done
                    StatusFilter.DONE    -> it.task.done
                }
            }
            .filter { priorityFilter == null || it.task.priority == priorityFilter }
    }

    val searchPlaceholder = stringResource(R.string.search_placeholder)
    val clearSearchCd = stringResource(R.string.cd_clear_search)

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = colors.searchBarColors.inputFieldColors,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = {
                Text(modifier = Modifier.clearAndSetSemantics {}, text = searchPlaceholder)
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.edit { replace(0, length, "") } }) {
                        Icon(Icons.Default.Close, contentDescription = clearSearchCd)
                    }
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = inputField,
                colors = colors,
                scrollBehavior = scrollBehavior,
            )
            ExpandedDockedSearchBarWithGap(
                state = searchBarState,
                inputField = inputField,
            ) {
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.no_search_results, query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchResults, key = { it.task.id }) { item ->
                            ListItem(
                                headlineContent = { Text(item.task.title) },
                                supportingContent = { Text(item.projectName) },
                                trailingContent = { PriorityOrCheckIcon(item) },
                                modifier = Modifier.clickable {
                                    onTaskClick(item.projectId, item.projectName, item.task.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.message, color = MaterialTheme.colorScheme.error) }

            is UiState.Success -> {
                if (allTasks.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.no_tasks_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(320.dp),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding() + 8.dp,
                            bottom = padding.calculateBottomPadding() + 12.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            FilterRow(
                                statusFilter = statusFilter,
                                onStatusFilterChange = { statusFilter = it },
                                priorityFilter = priorityFilter,
                                onPriorityFilterChange = { priorityFilter = it },
                            )
                        }

                        if (displayedTasks.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        stringResource(R.string.no_tasks_in_filter),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            gridItems(displayedTasks, key = { it.task.id }) { item ->
                                ListItem(
                                    headlineContent = { Text(item.task.title) },
                                    supportingContent = { Text(item.projectName) },
                                    trailingContent = { PriorityOrCheckIcon(item) },
                                    colors = itemColors,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onTaskClick(item.projectId, item.projectName, item.task.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityOrCheckIcon(item: TaskWithProject) {
    AnimatedContent(
        targetState = item.task.done,
        transitionSpec = { itemEnterTransition togetherWith itemExitTransition },
        label = "taskIcon_${item.task.id}",
    ) { isDone ->
        if (isDone) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(priorityColor(item.task.priority)),
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
}

@Composable
private fun FilterRow(
    statusFilter: StatusFilter,
    onStatusFilterChange: (StatusFilter) -> Unit,
    priorityFilter: Priority?,
    onPriorityFilterChange: (Priority?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusFilter.entries.forEach { option ->
                FilterChip(
                    selected = statusFilter == option,
                    onClick = { onStatusFilterChange(option) },
                    label = { Text(stringResource(option.labelRes)) },
                    leadingIcon = if (statusFilter == option) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = priorityFilter == null,
                onClick = { onPriorityFilterChange(null) },
                label = { Text(stringResource(R.string.filter_all)) },
                leadingIcon = if (priorityFilter == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
            )
            Priority.entries.forEach { priority ->
                FilterChip(
                    selected = priorityFilter == priority,
                    onClick = { onPriorityFilterChange(priority) },
                    label = { Text(stringResource(priority.labelRes)) },
                    leadingIcon = if (priorityFilter == priority) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
            }
        }
    }
}

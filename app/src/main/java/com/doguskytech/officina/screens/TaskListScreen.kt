package com.doguskytech.officina.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.ui.UiState
import kotlinx.coroutines.launch

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

    val query = textFieldState.text.toString()
    val allTasks = if (uiState is UiState.Success) uiState.data else emptyList()
    val filteredTasks = remember(query, allTasks) {
        if (query.isBlank()) allTasks
        else allTasks.filter { it.task.title.contains(query, ignoreCase = true) }
    }

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = colors.searchBarColors.inputFieldColors,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = {
                Text(modifier = Modifier.clearAndSetSemantics {}, text = "Buscar tarefa...")
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.edit { replace(0, length, "") } }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar busca")
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
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Nenhum resultado para \"$query\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredTasks, key = { it.task.id }) { item ->
                            ListItem(
                                headlineContent = { Text(item.task.title) },
                                supportingContent = { Text(item.projectName) },
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
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(uiState.message, color = MaterialTheme.colorScheme.error) }

            is UiState.Success -> {
                if (allTasks.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma tarefa ainda.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(320.dp),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding() + 12.dp,
                            bottom = padding.calculateBottomPadding() + 12.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        gridItems(allTasks, key = { it.task.id }) { item ->
                            ListItem(
                                selected = item.task.done,
                                onClick = {},
                                colors = itemColors,
                                supportingContent = { Text(item.projectName) },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(item.task.title) }
                        }
                    }
                }
            }
        }
    }
}

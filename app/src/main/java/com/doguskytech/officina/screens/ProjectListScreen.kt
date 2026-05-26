package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.ui.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProjectListScreen(
    uiState: UiState<List<Project>>,
    selectedProjectId: Int?,
    onProjectClick: (ProjectDetail) -> Unit,
    onSortClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oficina") },
                actions = {
                    IconButton(onClick = dropUnlessResumed { onSortClick() }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(uiState.message, color = MaterialTheme.colorScheme.error) }

            is UiState.Success -> {
                val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                items(uiState.data, key = { it.id }) { project ->
                    // Fase 2: ListItem(selected = ...) cuida do background, shape e animação
                    // automaticamente. Zero lógica manual de clip/background/cor.
                    ListItem(
                        selected = project.id == selectedProjectId,
                        onClick = dropUnlessResumed {
                            onProjectClick(ProjectDetail(project.id, project.name))
                        },
                        colors = colors,
                        supportingContent = { Text("${project.tasks.size} tarefas") },
                        trailingContent = {
                            Text("→", style = MaterialTheme.typography.titleLarge)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(project.name) }
                }
            }
            }
        }
    }
}

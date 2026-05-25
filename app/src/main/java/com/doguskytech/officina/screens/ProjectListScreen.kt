package com.doguskytech.officina.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.navigation.ProjectDetail

private data class Project(val id: Int, val name: String, val taskCount: Int)

private val sampleProjects = listOf(
    Project(1, "App Mobile", 5),
    Project(2, "API Backend", 3),
    Project(3, "Design System", 8),
    Project(4, "Documentação", 2),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    onProjectClick: (ProjectDetail) -> Unit,
    onSortClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oficina") },
                actions = {
                    IconButton(onClick = dropUnlessResumed { onSortClick() }) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sampleProjects) { project ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        // dropUnlessResumed evita duplo-clique durante a animação de transição
                        .clickable(onClick = dropUnlessResumed {
                            onProjectClick(
                                ProjectDetail(
                                    projectId = project.id,
                                    projectName = project.name
                                )
                            )
                        }),
                    headlineContent = { Text(project.name) },
                    supportingContent = { Text("${project.taskCount} tarefas") },
                    trailingContent = {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
            }
        }
    }
}

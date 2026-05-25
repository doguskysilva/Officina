package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.scenes.ListDetailSceneStrategy
import com.doguskytech.officina.scenes.rememberListDetailSceneStrategy
import com.doguskytech.officina.screens.ConfirmDeleteDialog
import com.doguskytech.officina.screens.NewTaskScreen
import com.doguskytech.officina.screens.ProjectDetailScreen
import com.doguskytech.officina.screens.ProjectListScreen
import com.doguskytech.officina.ui.theme.OfficinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficinaTheme {
                val backStack = rememberNavBackStack(ProjectList)

                // Strategies são avaliadas em ordem.
                // Dialog primeiro: se o último entry for diálogo, ele assume.
                // ListDetail depois: se a janela for larga e houver lista+detalhe, ele assume.
                // Fallback automático: SinglePaneSceneStrategy (sempre renderiza o último entry).
                val dialogStrategy = DialogSceneStrategy<NavKey>()
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategies = listOf(dialogStrategy, listDetailStrategy),
                    entryProvider = entryProvider<NavKey> {

                        // metadata = ListDetailSceneStrategy.listPane() → marca como painel de lista
                        entry<ProjectList>(
                            metadata = ListDetailSceneStrategy.listPane()
                        ) {
                            ProjectListScreen(
                                onProjectClick = { route ->
                                    // No tablet, ao trocar o item selecionado, removemos o detalhe
                                    // anterior para não acumular entries no back stack.
                                    backStack.removeIf { it is ProjectDetail }
                                    backStack.add(route)
                                }
                            )
                        }

                        // metadata = ListDetailSceneStrategy.detailPane() → marca como painel de detalhe
                        entry<ProjectDetail>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { route ->
                            ProjectDetailScreen(
                                projectId = route.projectId,
                                projectName = route.projectName,
                                onBack = { backStack.removeLastOrNull() },
                                onNewTaskClick = { newTaskRoute -> backStack.add(newTaskRoute) },
                                onDeleteClick = { confirmRoute -> backStack.add(confirmRoute) }
                            )
                        }

                        entry<NewTask> { route ->
                            NewTaskScreen(
                                projectId = route.projectId,
                                onBack = { backStack.removeLastOrNull() },
                                onSave = { backStack.removeLastOrNull() }
                            )
                        }

                        entry<ConfirmDelete>(
                            metadata = DialogSceneStrategy.dialog()
                        ) { route ->
                            ConfirmDeleteDialog(
                                projectName = route.projectName,
                                onConfirm = {
                                    backStack.removeLastOrNull() // ConfirmDelete
                                    backStack.removeLastOrNull() // ProjectDetail
                                },
                                onDismiss = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }
        }
    }
}

package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.screens.ConfirmDeleteDialog
import com.doguskytech.officina.screens.NewTaskScreen
import com.doguskytech.officina.screens.ProjectDetailScreen
import com.doguskytech.officina.screens.ProjectDetailPlaceholder
import com.doguskytech.officina.screens.ProjectListScreen
import com.doguskytech.officina.ui.theme.OfficinaTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficinaTheme {
                val backStack = rememberNavBackStack(ProjectList)

                val dialogStrategy = DialogSceneStrategy<NavKey>()

                // Material Adaptive version — usa WindowAdaptiveInfo internamente.
                // A diretiva remove o espaçamento horizontal entre painéis (workaround bug b/418201867).
                val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
                    directive = directive
                )

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategies = listOf(dialogStrategy, listDetailStrategy),
                    entryProvider = entryProvider<NavKey> {

                        entry<ProjectList>(
                            metadata = ListDetailSceneStrategy.listPane(
                                // Exibido no painel direito quando nenhum projeto está selecionado.
                                // Só aparece no tablet/janela larga — no phone não existe painel direito.
                                detailPlaceholder = { ProjectDetailPlaceholder() }
                            )
                        ) {
                            ProjectListScreen(
                                onProjectClick = { route ->
                                    backStack.removeIf { it is ProjectDetail }
                                    backStack.add(route)
                                }
                            )
                        }

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

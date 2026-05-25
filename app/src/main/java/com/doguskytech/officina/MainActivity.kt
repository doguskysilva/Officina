package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.doguskytech.officina.navigation.AppSettings
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.navigation.TaskList
import com.doguskytech.officina.scenes.appTabs
import com.doguskytech.officina.scenes.rememberNavDecoratorStrategy
import com.doguskytech.officina.screens.ConfirmDeleteDialog
import com.doguskytech.officina.screens.NewTaskScreen
import com.doguskytech.officina.screens.ProjectDetailPlaceholder
import com.doguskytech.officina.screens.ProjectDetailScreen
import com.doguskytech.officina.screens.ProjectListScreen
import com.doguskytech.officina.screens.SettingsScreen
import com.doguskytech.officina.screens.TaskListScreen
import com.doguskytech.officina.ui.theme.OfficinaTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficinaTheme {
                val backStack = rememberNavBackStack(ProjectList)

                // Aba selecionada = primeiro item top-level que encontrar no back stack.
                // derivedStateOf: só recomputa quando o back stack muda, evita recomposições desnecessárias.
                val topLevelRoutes = remember { appTabs.map { it.route }.toSet() }
                val selectedTab by remember {
                    derivedStateOf {
                        backStack.firstOrNull { it in topLevelRoutes } ?: ProjectList
                    }
                }

                val dialogStrategy = DialogSceneStrategy<NavKey>()

                val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
                    directive = directive
                )

                // SceneDecoratorStrategy — envolve cada Scene com NavigationBar ou NavigationRail.
                // A ordem em sceneDecoratorStrategies define a ordem de envolvimento (de dentro para fora).
                val navDecorator = rememberNavDecoratorStrategy<NavKey>(
                    selectedTab = selectedTab,
                    onTabSelected = { route ->
                        // Módulo 3: troca simples — limpa o back stack e vai para a aba.
                        // Perde o histórico de navegação de cada aba.
                        // Módulo 4 vai preservar o estado de cada aba com múltiplos back stacks.
                        backStack.clear()
                        backStack.add(route)
                    }
                )

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategies = listOf(dialogStrategy, listDetailStrategy),
                    // sceneDecoratorStrategies é aplicado APÓS as sceneStrategies.
                    // Cada decorator recebe a Scene já calculada e a envolve.
                    sceneDecoratorStrategies = listOf(navDecorator),
                    entryProvider = entryProvider<NavKey> {

                        entry<ProjectList>(
                            metadata = ListDetailSceneStrategy.listPane(
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

                        entry<NewTask>(
                            // Phone/tablet médio: tela cheia normal
                            // Tablet largo (expanded): terceiro painel ao lado do detalhe
                            metadata = ListDetailSceneStrategy.extraPane()
                        ) { route ->
                            NewTaskScreen(
                                projectId = route.projectId,
                                onBack = { backStack.removeLastOrNull() },
                                onSave = { backStack.removeLastOrNull() }
                            )
                        }

                        entry<TaskList> { TaskListScreen() }

                        entry<AppSettings> { SettingsScreen() }

                        // OverlayScene — o decorator NÃO é aplicado aqui.
                        // Diálogos nunca recebem NavigationBar/Rail.
                        entry<ConfirmDelete>(
                            metadata = DialogSceneStrategy.dialog()
                        ) { route ->
                            ConfirmDeleteDialog(
                                projectName = route.projectName,
                                onConfirm = {
                                    backStack.removeLastOrNull()
                                    backStack.removeLastOrNull()
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

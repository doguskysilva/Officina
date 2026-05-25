package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                // Módulo 4: um back stack por aba — cada aba preserva seu histórico.
                val projectsBackStack = rememberNavBackStack(ProjectList)
                val tasksBackStack = rememberNavBackStack(TaskList)
                val settingsBackStack = rememberNavBackStack(AppSettings)

                // Aba selecionada agora é estado explícito — não precisa mais de derivedStateOf.
                var selectedTab by remember { mutableStateOf<NavKey>(ProjectList) }

                // Back stack ativo = o da aba selecionada.
                val activeBackStack = when (selectedTab) {
                    ProjectList -> projectsBackStack
                    TaskList -> tasksBackStack
                    else -> settingsBackStack
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
                        // Módulo 4: só troca a aba — o back stack de cada aba é preservado.
                        selectedTab = route
                    }
                )

                // Fallback para o sistema de back no tablet: quando ListDetailSceneStrategy
                // agrupa tudo numa única Scene (previousEntries=[]), NavDisplay não habilita
                // o back nativo — este handler cobre esse caso.
                BackHandler(enabled = activeBackStack.size > 1) {
                    activeBackStack.removeLastOrNull()
                }

                NavDisplay(
                    backStack = activeBackStack,
                    onBack = { activeBackStack.removeLastOrNull() },
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
                                    // Remove detalhe e nova tarefa do projeto anterior antes de abrir o novo.
                                    activeBackStack.removeIf { it is ProjectDetail || it is NewTask }
                                    activeBackStack.add(route)
                                }
                            )
                        }

                        entry<ProjectDetail>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { route ->
                            ProjectDetailScreen(
                                projectId = route.projectId,
                                projectName = route.projectName,
                                onBack = { activeBackStack.removeLastOrNull() },
                                onNewTaskClick = { newTaskRoute -> activeBackStack.add(newTaskRoute) },
                                onDeleteClick = { confirmRoute -> activeBackStack.add(confirmRoute) }
                            )
                        }

                        entry<NewTask>(
                            // Phone/tablet médio: tela cheia normal
                            // Tablet largo (expanded): terceiro painel ao lado do detalhe
                            metadata = ListDetailSceneStrategy.extraPane()
                        ) { route ->
                            NewTaskScreen(
                                projectId = route.projectId,
                                onBack = { activeBackStack.removeLastOrNull() },
                                onSave = { activeBackStack.removeLastOrNull() }
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
                                    activeBackStack.removeLastOrNull()
                                    activeBackStack.removeLastOrNull()
                                },
                                onDismiss = { activeBackStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }
        }
    }
}

package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.doguskytech.officina.data.ProjectRepository
import com.doguskytech.officina.navigation.AppSettings
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.navigation.SortProjects
import com.doguskytech.officina.navigation.TaskList
import com.doguskytech.officina.scenes.BottomSheetSceneStrategy
import com.doguskytech.officina.scenes.rememberBottomSheetSceneStrategy
import com.doguskytech.officina.screens.ConfirmDeleteDialog
import com.doguskytech.officina.screens.NewTaskScreen
import com.doguskytech.officina.screens.ProjectDetailPlaceholder
import com.doguskytech.officina.screens.ProjectDetailScreen
import com.doguskytech.officina.screens.ProjectListScreen
import com.doguskytech.officina.screens.SettingsScreen
import com.doguskytech.officina.screens.SortProjectsSheet
import com.doguskytech.officina.screens.TaskListScreen
import com.doguskytech.officina.ui.theme.OfficinaTheme
import com.doguskytech.officina.viewmodel.ProjectDetailViewModel
import com.doguskytech.officina.viewmodel.ProjectListViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficinaTheme {
                val projectsBackStack = rememberNavBackStack(ProjectList)
                val tasksBackStack = rememberNavBackStack(TaskList)
                val settingsBackStack = rememberNavBackStack(AppSettings)

                var selectedTab by remember { mutableStateOf<NavKey>(ProjectList) }

                val activeBackStack = when (selectedTab) {
                    ProjectList -> projectsBackStack
                    TaskList -> tasksBackStack
                    else -> settingsBackStack
                }

                val dialogStrategy = DialogSceneStrategy<NavKey>()
                val bottomSheetStrategy = rememberBottomSheetSceneStrategy<NavKey>()

                val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
                    directive = directive
                )

                // Fallback para o sistema de back no tablet: quando ListDetailSceneStrategy
                // agrupa tudo numa única Scene (previousEntries=[]), NavDisplay não habilita
                // o back nativo — este handler cobre esse caso.
                BackHandler(enabled = activeBackStack.size > 1) {
                    activeBackStack.removeLastOrNull()
                }

                // Módulo 6: NavigationSuiteScaffold substitui nosso NavDecoratorStrategy.
                // Phone → NavigationBar (base), tablet → NavigationRail (lateral),
                // desktop → NavigationDrawer — tudo automático via WindowAdaptiveInfo.
                NavigationSuiteScaffold(
                    navigationItems = {
                        NavigationSuiteItem(
                            icon = { Icon(Icons.Default.Build, contentDescription = null) },
                            label = { Text("Projetos") },
                            selected = selectedTab == ProjectList,
                            onClick = { selectedTab = ProjectList },
                        )
                        NavigationSuiteItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            label = { Text("Tarefas") },
                            selected = selectedTab == TaskList,
                            onClick = { selectedTab = TaskList },
                        )
                        NavigationSuiteItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Ajustes") },
                            selected = selectedTab == AppSettings,
                            onClick = { selectedTab = AppSettings },
                        )
                    }
                ) {
                    NavDisplay(
                        backStack = activeBackStack,
                        onBack = { activeBackStack.removeLastOrNull() },
                        sceneStrategies = listOf(dialogStrategy, bottomSheetStrategy, listDetailStrategy),
                        entryProvider = entryProvider<NavKey> {

                            entry<ProjectList>(
                                metadata = ListDetailSceneStrategy.listPane(
                                    detailPlaceholder = { ProjectDetailPlaceholder() }
                                )
                            ) {
                                // Módulo 7: viewModel() funciona aqui porque cada entry do NavDisplay
                                // é um ViewModelStoreOwner independente (via lifecycle-viewmodel-navigation3).
                                // O ViewModel é destruído quando o entry sai do back stack.
                                val vm: ProjectListViewModel = viewModel()
                                val uiState by vm.uiState.collectAsStateWithLifecycle()
                                ProjectListScreen(
                                    uiState = uiState,
                                    onProjectClick = { route ->
                                        activeBackStack.removeIf { it is ProjectDetail || it is NewTask }
                                        activeBackStack.add(route)
                                    },
                                    onSortClick = { activeBackStack.add(SortProjects) }
                                )
                            }

                            entry<ProjectDetail>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) { route ->
                                // key = projectId garante um ViewModel distinto por projeto.
                                // Sem isso, o Compose reutiliza o ViewModel quando a Scene troca
                                // de ProjectDetail(1) para ProjectDetail(2) sem resetar o subtree.
                                val vm: ProjectDetailViewModel = viewModel(key = route.projectId.toString()) {
                                    ProjectDetailViewModel(route.projectId)
                                }
                                val uiState by vm.uiState.collectAsStateWithLifecycle()
                                ProjectDetailScreen(
                                    uiState = uiState,
                                    onBack = { activeBackStack.removeLastOrNull() },
                                    onNewTaskClick = { newTaskRoute -> activeBackStack.add(newTaskRoute) },
                                    onDeleteClick = { confirmRoute -> activeBackStack.add(confirmRoute) }
                                )
                            }

                            entry<NewTask>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) { route ->
                                NewTaskScreen(
                                    projectId = route.projectId,
                                    onBack = { activeBackStack.removeLastOrNull() },
                                    onSave = { title ->
                                        ProjectRepository.addTask(route.projectId, title)
                                        activeBackStack.removeLastOrNull()
                                    }
                                )
                            }

                            entry<SortProjects>(
                                metadata = BottomSheetSceneStrategy.bottomSheet()
                            ) {
                                SortProjectsSheet()
                            }

                            entry<TaskList> { TaskListScreen() }

                            entry<AppSettings> { SettingsScreen() }

                            // OverlayScene — NavigationSuiteScaffold não envolve diálogos
                            // (OverlayScene não recebe SceneDecorator, então a NavBar/Rail
                            //  nunca aparece dentro de Dialog ou BottomSheet).
                            entry<ConfirmDelete>(
                                metadata = DialogSceneStrategy.dialog()
                            ) { route ->
                                ConfirmDeleteDialog(
                                    projectName = route.projectName,
                                    onConfirm = {
                                        ProjectRepository.deleteProject(route.projectId)
                                        activeBackStack.removeLastOrNull() // remove ConfirmDelete
                                        activeBackStack.removeLastOrNull() // remove ProjectDetail
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
}

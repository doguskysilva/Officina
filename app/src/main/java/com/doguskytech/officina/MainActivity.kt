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
import androidx.compose.animation.AnimatedContent
import com.doguskytech.officina.ui.AnimationConfig
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.doguskytech.officina.R
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
import com.doguskytech.officina.data.InMemoryProjectRepository
import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.navigation.AppSettings
import com.doguskytech.officina.navigation.ConfirmDelete
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.navigation.NewProject
import com.doguskytech.officina.navigation.SortProjects
import com.doguskytech.officina.navigation.TaskList
import com.doguskytech.officina.scenes.BottomSheetSceneStrategy
import com.doguskytech.officina.scenes.rememberBottomSheetSceneStrategy
import com.doguskytech.officina.screens.ConfirmDeleteDialog
import com.doguskytech.officina.screens.NewTaskDialog
import com.doguskytech.officina.screens.NewProjectDialog
import com.doguskytech.officina.screens.NewProjectScreen
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
import com.doguskytech.officina.viewmodel.SortProjectsViewModel
import com.doguskytech.officina.viewmodel.TaskListViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkProjectId = intent.getIntExtra("project_id", -1).takeIf { it != -1 }
        val deepLinkProjectName = intent.getStringExtra("project_name")

        setContent {
            OfficinaTheme {
                val projectsInitialStack: Array<NavKey> = if (
                    savedInstanceState == null &&
                    deepLinkProjectId != null &&
                    deepLinkProjectName != null
                ) {
                    arrayOf(ProjectList, ProjectDetail(deepLinkProjectId, deepLinkProjectName))
                } else {
                    arrayOf(ProjectList)
                }
                val projectsBackStack = rememberNavBackStack(*projectsInitialStack)
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
                val isMultiPane = directive.maxHorizontalPartitions > 1
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
                    directive = directive
                )

                BackHandler(enabled = activeBackStack.size > 1) {
                    activeBackStack.removeLastOrNull()
                }

                NavigationSuiteScaffold(
                    navigationItems = {
                        NavigationSuiteItem(
                            icon = { Icon(Icons.Default.Build, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_projects)) },
                            selected = selectedTab == ProjectList,
                            onClick = { selectedTab = ProjectList },
                        )
                        NavigationSuiteItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_tasks)) },
                            selected = selectedTab == TaskList,
                            onClick = { selectedTab = TaskList },
                        )
                        NavigationSuiteItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_settings)) },
                            selected = selectedTab == AppSettings,
                            onClick = { selectedTab = AppSettings },
                        )
                    }
                ) {
                    NavDisplay(
                        backStack = activeBackStack,
                        onBack = { activeBackStack.removeLastOrNull() },
                        sceneStrategies = listOf(dialogStrategy, bottomSheetStrategy, listDetailStrategy),
                        transitionSpec = { AnimationConfig.current.enter },
                        popTransitionSpec = { AnimationConfig.current.pop },
                        entryProvider = entryProvider<NavKey> {

                            entry<ProjectList>(
                                metadata = ListDetailSceneStrategy.listPane(
                                    detailPlaceholder = { ProjectDetailPlaceholder() }
                                )
                            ) {
                                val vm: ProjectListViewModel = viewModel()
                                val uiState by vm.uiState.collectAsStateWithLifecycle()
                                val selectedProjectId = activeBackStack
                                    .filterIsInstance<ProjectDetail>()
                                    .lastOrNull()?.projectId
                                ProjectListScreen(
                                    uiState = uiState,
                                    selectedProjectId = selectedProjectId,
                                    onProjectClick = { route ->
                                        activeBackStack.removeIf { it is ProjectDetail || it is NewTask }
                                        activeBackStack.add(route)
                                    },
                                    onSortClick = { activeBackStack.add(SortProjects) },
                                    onNewProjectClick = { activeBackStack.add(NewProject) }
                                )
                            }

                            entry<ProjectDetail>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) { route ->
                                val vm: ProjectDetailViewModel = viewModel(key = route.projectId.toString()) {
                                    ProjectDetailViewModel(route.projectId)
                                }
                                val uiState by vm.uiState.collectAsStateWithLifecycle()
                                AnimatedContent(
                                    targetState = route.projectId,
                                    transitionSpec = { AnimationConfig.current.enter },
                                    label = "ProjectDetailTransition",
                                ) { _ ->
                                    ProjectDetailScreen(
                                        uiState = uiState,
                                        showBackButton = !isMultiPane,
                                        onBack = { activeBackStack.removeLastOrNull() },
                                        onNewTaskClick = { newTaskRoute -> activeBackStack.add(newTaskRoute) },
                                        onDeleteClick = { confirmRoute -> activeBackStack.add(confirmRoute) },
                                        onMarkAllDone = { vm.markAllTasksDone() },
                                        onCompleteTasks = { ids -> vm.completeTasks(ids) },
                                        onDeleteTasks = { ids -> vm.deleteTasks(ids) },
                                        onCancelTasks = { ids -> vm.cancelTasks(ids) },
                                        onStartProject = { vm.startProject() },
                                        onFinishProject = { vm.finishProject() },
                                        onCancelProject = { vm.cancelProject() },
                                        highlightTaskId = route.highlightTaskId,
                                    )
                                }
                            }

                            entry<NewTask>(
                                metadata = if (isMultiPane) DialogSceneStrategy.dialog()
                                           else emptyMap()
                            ) { route ->
                                val saveTask: (String, Priority) -> Unit = { t, p ->
                                    InMemoryProjectRepository.addTask(route.projectId, t, p)
                                    activeBackStack.removeLastOrNull()
                                }
                                if (isMultiPane) {
                                    NewTaskDialog(
                                        onBack = { activeBackStack.removeLastOrNull() },
                                        onSave = saveTask,
                                    )
                                } else {
                                    NewTaskScreen(
                                        projectId = route.projectId,
                                        onBack = { activeBackStack.removeLastOrNull() },
                                        onSave = saveTask,
                                    )
                                }
                            }

                            entry<SortProjects>(
                                metadata = BottomSheetSceneStrategy.bottomSheet()
                            ) {
                                val vm: SortProjectsViewModel = viewModel()
                                val currentSort by vm.currentSort.collectAsStateWithLifecycle()
                                SortProjectsSheet(
                                    currentSort = currentSort,
                                    onSortChange = { vm.setSort(it) },
                                )
                            }

                            entry<NewProject>(
                                metadata = if (isMultiPane) DialogSceneStrategy.dialog()
                                           else emptyMap()
                            ) {
                                val saveProject: (String) -> Unit = { name ->
                                    InMemoryProjectRepository.addProject(name)
                                    activeBackStack.removeLastOrNull()
                                }
                                if (isMultiPane) {
                                    NewProjectDialog(
                                        onBack = { activeBackStack.removeLastOrNull() },
                                        onSave = saveProject,
                                    )
                                } else {
                                    NewProjectScreen(
                                        onBack = { activeBackStack.removeLastOrNull() },
                                        onSave = saveProject,
                                    )
                                }
                            }

                            entry<TaskList> {
                                val vm: TaskListViewModel = viewModel()
                                val uiState by vm.uiState.collectAsStateWithLifecycle()
                                TaskListScreen(
                                    uiState = uiState,
                                    onTaskClick = { projectId, projectName, taskId ->
                                        selectedTab = ProjectList
                                        projectsBackStack.removeIf { it is ProjectDetail || it is NewTask }
                                        projectsBackStack.add(ProjectDetail(projectId, projectName, highlightTaskId = taskId))
                                    }
                                )
                            }

                            entry<AppSettings> { SettingsScreen() }

                            entry<ConfirmDelete>(
                                metadata = DialogSceneStrategy.dialog()
                            ) { route ->
                                ConfirmDeleteDialog(
                                    projectName = route.projectName,
                                    onConfirm = {
                                        InMemoryProjectRepository.deleteProject(route.projectId)
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
}

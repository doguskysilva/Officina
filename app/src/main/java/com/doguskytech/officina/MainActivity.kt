package com.doguskytech.officina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.doguskytech.officina.navigation.NewTask
import com.doguskytech.officina.navigation.ProjectDetail
import com.doguskytech.officina.navigation.ProjectList
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
                // rememberNavBackStack:
                // - persiste rotação e process death (usa rememberSaveable internamente)
                // - exige @Serializable nos NavKeys
                // - começa com ProjectList como destino inicial
                val backStack = rememberNavBackStack(ProjectList)

                NavDisplay(
                    backStack = backStack,
                    // onBack = o que acontece quando o sistema dispara "voltar"
                    // removeLastOrNull = não lança exceção se a lista estiver vazia
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider<NavKey> {

                        entry<ProjectList> {
                            ProjectListScreen(
                                onProjectClick = { route -> backStack.add(route) }
                            )
                        }

                        entry<ProjectDetail> { route ->
                            // `route` aqui já é ProjectDetail com os args type-safe
                            ProjectDetailScreen(
                                projectId = route.projectId,
                                projectName = route.projectName,
                                onBack = { backStack.removeLastOrNull() },
                                onNewTaskClick = { newTaskRoute -> backStack.add(newTaskRoute) }
                            )
                        }

                        entry<NewTask> { route ->
                            NewTaskScreen(
                                projectId = route.projectId,
                                onBack = { backStack.removeLastOrNull() },
                                onSave = { taskName ->
                                    // Por ora só volta. No Módulo 4 retornaremos resultado real.
                                    backStack.removeLastOrNull()
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

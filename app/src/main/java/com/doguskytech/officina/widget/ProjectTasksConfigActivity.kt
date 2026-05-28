package com.doguskytech.officina.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.doguskytech.officina.data.ProjectRepository
import com.doguskytech.officina.ui.theme.OfficinaTheme
import kotlinx.coroutines.launch

class ProjectTasksConfigActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appWidgetId = intent.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Resultado padrão CANCELLED — só muda para OK após escolha do usuário
        setResult(RESULT_CANCELED)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            OfficinaTheme {
                val projects by ProjectRepository.projects.collectAsStateWithLifecycle()

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Escolher projeto") })
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        items(projects) { project ->
                            ListItem(
                                headlineContent = { Text(project.name) },
                                supportingContent = { Text("${project.tasks.size} tarefas") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        lifecycleScope.launch {
                                            saveSelection(appWidgetId, project.id)
                                        }
                                    },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveSelection(appWidgetId: Int, projectId: Int) {
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        updateAppWidgetState(this, glanceId) { prefs ->
            prefs[ProjectTasksWidget.PROJECT_ID_KEY] = projectId
        }
        ProjectTasksWidget().update(this, glanceId)
        setResult(
            RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}

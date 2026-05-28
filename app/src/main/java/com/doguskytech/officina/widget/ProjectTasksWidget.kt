package com.doguskytech.officina.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.action.clickable
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.doguskytech.officina.R
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.data.ProjectRepository
import com.doguskytech.officina.data.Task
import kotlinx.coroutines.flow.first

class ProjectTasksWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    companion object {
        val PROJECT_ID_KEY = intPreferencesKey("selected_project_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val projects = ProjectRepository.projects.first()

        provideContent {
            GlanceTheme {
                val projectId = currentState<Preferences>()[PROJECT_ID_KEY]
                val project = projects.find { it.id == projectId }
                Content(project = project)
            }
        }
    }

    @Composable
    private fun Content(project: Project?) {
        if (project == null) {
            NotConfiguredContent()
            return
        }
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface),
        ) {
            Text(
                text = project.name,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(project.tasks, itemId = { it.id.toLong() }) { task ->
                    TaskRow(projectId = project.id, task = task)
                }
            }
        }
    }

    @Composable
    private fun TaskRow(projectId: Int, task: Task) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionRunCallback<ToggleTaskAction>(
                        actionParametersOf(
                            ToggleTaskAction.TASK_ID_KEY to task.id,
                            ToggleTaskAction.PROJECT_ID_KEY to projectId,
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(
                    if (task.done) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
                ),
                contentDescription = null,
                colorFilter = androidx.glance.ColorFilter.tint(
                    if (task.done) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant
                ),
                modifier = GlanceModifier.size(20.dp),
            )
            Text(
                text = task.title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                ),
                modifier = GlanceModifier.padding(start = 10.dp),
            )
        }
    }

    @Composable
    private fun NotConfiguredContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Toque para configurar",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                ),
            )
        }
    }
}

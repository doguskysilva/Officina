
package com.doguskytech.officina.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.doguskytech.officina.MainActivity
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.data.ProjectRepository
import kotlinx.coroutines.flow.first

class ProjectListWidget : GlanceAppWidget() {

    companion object {
        val PROJECT_ID_KEY = ActionParameters.Key<Int>("project_id")
        val PROJECT_NAME_KEY = ActionParameters.Key<String>("project_name")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val projects = ProjectRepository.projects.first()

        provideContent {
            GlanceTheme {
                Content(projects = projects)
            }
        }
    }

    @Composable
    private fun Content(projects: List<Project>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface),
        ) {
            Text(
                text = "Projetos",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(projects, itemId = { it.id.toLong() }) { project ->
                    val total = project.tasks.size
                    val done = project.tasks.count { it.done }
                    val progress = if (total == 0) 0f else done.toFloat() / total
                    ProjectRow(project = project, done = done, total = total, progress = progress)
                }
            }
        }
    }

    @Composable
    private fun ProjectRow(project: Project, done: Int, total: Int, progress: Float) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionStartActivity<MainActivity>(
                        actionParametersOf(
                            PROJECT_ID_KEY to project.id,
                            PROJECT_NAME_KEY to project.name,
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = project.name,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp),
                )
                Text(
                    text = "$done/$total concluídas",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 11.sp,
                    ),
                    modifier = GlanceModifier.padding(top = 4.dp),
                )
            }
        }
    }
}

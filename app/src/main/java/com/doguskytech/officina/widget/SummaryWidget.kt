package com.doguskytech.officina.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.provideContent
import com.doguskytech.officina.R
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.doguskytech.officina.MainActivity
import com.doguskytech.officina.data.ProjectRepository
import kotlinx.coroutines.flow.first

class SummaryWidget : GlanceAppWidget() {

    companion object {
        private val smallSize = DpSize(110.dp, 50.dp)
        private val mediumSize = DpSize(180.dp, 180.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(smallSize, mediumSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val projects = ProjectRepository.projects.first()
        val projectCount = projects.size
        val pendingCount = projects.flatMap { it.tasks }.count { !it.done }

        provideContent {
            GlanceTheme {
                Content(projectCount = projectCount, pendingCount = pendingCount)
            }
        }
    }

    @Composable
    private fun Content(projectCount: Int, pendingCount: Int) {
        val size = LocalSize.current
        if (size.height >= mediumSize.height) {
            MediumContent(projectCount, pendingCount)
        } else {
            SmallContent(projectCount, pendingCount)
        }
    }

    // Layout 2×1: Row com contagens à esquerda e botão à direita
    @Composable
    private fun SmallContent(projectCount: Int, pendingCount: Int) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "$projectCount projetos",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "$pendingCount tasks pendentes",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 12.sp,
                    ),
                )
            }
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_launch),
                contentDescription = "Abrir app",
                onClick = actionStartActivity<MainActivity>(),
            )
        }
    }

    // Layout 2×2+: Column centralizada com título, contagens e botão
    @Composable
    private fun MediumContent(projectCount: Int, pendingCount: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Officina",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "$projectCount projetos",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                ),
                modifier = GlanceModifier.padding(top = 12.dp),
            )
            Text(
                text = "$pendingCount tasks pendentes",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                ),
                modifier = GlanceModifier.padding(top = 4.dp),
            )
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_launch),
                contentDescription = "Abrir app",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.padding(top = 16.dp),
            )
        }
    }
}

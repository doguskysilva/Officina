package com.doguskytech.officina.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.doguskytech.officina.data.ProjectRepository

class ToggleTaskAction : ActionCallback {

    companion object {
        val TASK_ID_KEY = ActionParameters.Key<Int>("task_id")
        val PROJECT_ID_KEY = ActionParameters.Key<Int>("project_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[TASK_ID_KEY] ?: return
        val projectId = parameters[PROJECT_ID_KEY] ?: return
        ProjectRepository.toggleTask(projectId, taskId)
        ProjectTasksWidget().update(context, glanceId)
    }
}

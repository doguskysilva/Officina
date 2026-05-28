package com.doguskytech.officina.domain.rules

import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus

object ProjectRules {
    fun canAddTask(project: Project): Boolean = project.isActive
    fun canCompleteTasks(project: Project): Boolean = project.isActive
    fun canCancelTasks(project: Project): Boolean = project.isActive
    fun canStart(project: Project): Boolean = project.status == ProjectStatus.WAITING
    fun canFinish(project: Project): Boolean = project.isActive && project.canFinish
    fun canCancel(project: Project): Boolean = project.status != ProjectStatus.DONE
    fun canDelete(project: Project): Boolean = !project.isActive
}

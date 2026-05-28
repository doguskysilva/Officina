package com.doguskytech.officina.domain

import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.Task
import com.doguskytech.officina.domain.model.TaskStatus
import com.doguskytech.officina.domain.rules.ProjectRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectRulesTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun project(
        status: ProjectStatus,
        tasks: List<Task> = emptyList(),
    ) = Project(id = 1, name = "Test", status = status, tasks = tasks)

    private fun pendingTask(id: Int = 1) =
        Task(id, "task $id", TaskStatus.PENDING, Priority.MEDIUM)

    private fun doneTask(id: Int = 1) =
        Task(id, "task $id", TaskStatus.DONE, Priority.MEDIUM)

    // ── canAddTask ────────────────────────────────────────────────────────────

    @Test fun `canAddTask returns true for IN_PROGRESS project`() {
        assertTrue(ProjectRules.canAddTask(project(ProjectStatus.IN_PROGRESS)))
    }

    @Test fun `canAddTask returns false for WAITING project`() {
        assertFalse(ProjectRules.canAddTask(project(ProjectStatus.WAITING)))
    }

    @Test fun `canAddTask returns false for DONE project`() {
        assertFalse(ProjectRules.canAddTask(project(ProjectStatus.DONE)))
    }

    @Test fun `canAddTask returns false for CANCELLED project`() {
        assertFalse(ProjectRules.canAddTask(project(ProjectStatus.CANCELLED)))
    }

    // ── canCompleteTasks ──────────────────────────────────────────────────────

    @Test fun `canCompleteTasks returns true only for IN_PROGRESS`() {
        assertTrue(ProjectRules.canCompleteTasks(project(ProjectStatus.IN_PROGRESS)))
        assertFalse(ProjectRules.canCompleteTasks(project(ProjectStatus.WAITING)))
        assertFalse(ProjectRules.canCompleteTasks(project(ProjectStatus.DONE)))
        assertFalse(ProjectRules.canCompleteTasks(project(ProjectStatus.CANCELLED)))
    }

    // ── canCancelTasks ────────────────────────────────────────────────────────

    @Test fun `canCancelTasks returns true only for IN_PROGRESS`() {
        assertTrue(ProjectRules.canCancelTasks(project(ProjectStatus.IN_PROGRESS)))
        assertFalse(ProjectRules.canCancelTasks(project(ProjectStatus.WAITING)))
        assertFalse(ProjectRules.canCancelTasks(project(ProjectStatus.DONE)))
        assertFalse(ProjectRules.canCancelTasks(project(ProjectStatus.CANCELLED)))
    }

    // ── canStart ──────────────────────────────────────────────────────────────

    @Test fun `canStart returns true only for WAITING`() {
        assertTrue(ProjectRules.canStart(project(ProjectStatus.WAITING)))
        assertFalse(ProjectRules.canStart(project(ProjectStatus.IN_PROGRESS)))
        assertFalse(ProjectRules.canStart(project(ProjectStatus.DONE)))
        assertFalse(ProjectRules.canStart(project(ProjectStatus.CANCELLED)))
    }

    // ── canFinish ─────────────────────────────────────────────────────────────

    @Test fun `canFinish returns true for IN_PROGRESS with all tasks done`() {
        val p = project(ProjectStatus.IN_PROGRESS, tasks = listOf(doneTask(1), doneTask(2)))
        assertTrue(ProjectRules.canFinish(p))
    }

    @Test fun `canFinish returns false for IN_PROGRESS with pending tasks`() {
        val p = project(ProjectStatus.IN_PROGRESS, tasks = listOf(doneTask(1), pendingTask(2)))
        assertFalse(ProjectRules.canFinish(p))
    }

    @Test fun `canFinish returns false for IN_PROGRESS with no tasks`() {
        assertFalse(ProjectRules.canFinish(project(ProjectStatus.IN_PROGRESS)))
    }

    @Test fun `canFinish returns false for non-IN_PROGRESS projects`() {
        val tasks = listOf(doneTask())
        assertFalse(ProjectRules.canFinish(project(ProjectStatus.WAITING, tasks)))
        assertFalse(ProjectRules.canFinish(project(ProjectStatus.DONE, tasks)))
        assertFalse(ProjectRules.canFinish(project(ProjectStatus.CANCELLED, tasks)))
    }

    // ── canCancel ─────────────────────────────────────────────────────────────

    @Test fun `canCancel returns false only for DONE`() {
        assertTrue(ProjectRules.canCancel(project(ProjectStatus.WAITING)))
        assertTrue(ProjectRules.canCancel(project(ProjectStatus.IN_PROGRESS)))
        assertTrue(ProjectRules.canCancel(project(ProjectStatus.CANCELLED)))
        assertFalse(ProjectRules.canCancel(project(ProjectStatus.DONE)))
    }

    // ── canDelete ─────────────────────────────────────────────────────────────

    @Test fun `canDelete returns false only for IN_PROGRESS`() {
        assertTrue(ProjectRules.canDelete(project(ProjectStatus.WAITING)))
        assertTrue(ProjectRules.canDelete(project(ProjectStatus.DONE)))
        assertTrue(ProjectRules.canDelete(project(ProjectStatus.CANCELLED)))
        assertFalse(ProjectRules.canDelete(project(ProjectStatus.IN_PROGRESS)))
    }
}

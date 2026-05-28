package com.doguskytech.officina.domain

import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.Task
import com.doguskytech.officina.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectModelTest {

    // ── Task computed properties ──────────────────────────────────────────────

    @Test fun `Task done is true only when status is DONE`() {
        assertTrue(Task(1, "t", TaskStatus.DONE, Priority.MEDIUM).done)
        assertFalse(Task(1, "t", TaskStatus.PENDING, Priority.MEDIUM).done)
        assertFalse(Task(1, "t", TaskStatus.CANCELLED, Priority.MEDIUM).done)
    }

    @Test fun `Task isPending is true only when status is PENDING`() {
        assertTrue(Task(1, "t", TaskStatus.PENDING, Priority.MEDIUM).isPending)
        assertFalse(Task(1, "t", TaskStatus.DONE, Priority.MEDIUM).isPending)
        assertFalse(Task(1, "t", TaskStatus.CANCELLED, Priority.MEDIUM).isPending)
    }

    @Test fun `Task defaults to PENDING status`() {
        val task = Task(id = 1, title = "default")
        assertTrue(task.isPending)
        assertFalse(task.done)
        assertNull(task.completedAt)
    }

    // ── Project computed properties ───────────────────────────────────────────

    @Test fun `Project pendingCount counts only PENDING tasks`() {
        val project = Project(
            id = 1, name = "p",
            tasks = listOf(
                Task(1, "t1", TaskStatus.PENDING, Priority.HIGH),
                Task(2, "t2", TaskStatus.DONE, Priority.HIGH),
                Task(3, "t3", TaskStatus.CANCELLED, Priority.HIGH),
                Task(4, "t4", TaskStatus.PENDING, Priority.LOW),
            )
        )
        assertEquals(2, project.pendingCount)
    }

    @Test fun `Project pendingCount is zero for empty task list`() {
        assertEquals(0, Project(id = 1, name = "p").pendingCount)
    }

    @Test fun `Project canFinish is true when all tasks are non-pending and list is non-empty`() {
        val project = Project(
            id = 1, name = "p",
            status = ProjectStatus.IN_PROGRESS,
            tasks = listOf(
                Task(1, "t1", TaskStatus.DONE, Priority.HIGH),
                Task(2, "t2", TaskStatus.CANCELLED, Priority.LOW),
            )
        )
        assertTrue(project.canFinish)
    }

    @Test fun `Project canFinish is false when there are pending tasks`() {
        val project = Project(
            id = 1, name = "p",
            status = ProjectStatus.IN_PROGRESS,
            tasks = listOf(
                Task(1, "t1", TaskStatus.DONE, Priority.HIGH),
                Task(2, "t2", TaskStatus.PENDING, Priority.LOW),
            )
        )
        assertFalse(project.canFinish)
    }

    @Test fun `Project canFinish is false when task list is empty`() {
        assertFalse(Project(id = 1, name = "p", status = ProjectStatus.IN_PROGRESS).canFinish)
    }

    @Test fun `Project isActive is true only for IN_PROGRESS status`() {
        assertTrue(Project(id = 1, name = "p", status = ProjectStatus.IN_PROGRESS).isActive)
        assertFalse(Project(id = 1, name = "p", status = ProjectStatus.WAITING).isActive)
        assertFalse(Project(id = 1, name = "p", status = ProjectStatus.DONE).isActive)
        assertFalse(Project(id = 1, name = "p", status = ProjectStatus.CANCELLED).isActive)
    }

    @Test fun `Project defaults to WAITING status`() {
        val project = Project(id = 1, name = "new project")
        assertEquals(ProjectStatus.WAITING, project.status)
        assertFalse(project.isActive)
    }
}

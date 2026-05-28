package com.doguskytech.officina.data

import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.SortOrder
import com.doguskytech.officina.domain.model.Task
import com.doguskytech.officina.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryProjectRepositoryTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun pendingTask(id: Int) = Task(id, "task $id", TaskStatus.PENDING, Priority.MEDIUM)
    private fun doneTask(id: Int) = Task(id, "task $id", TaskStatus.DONE, Priority.MEDIUM)

    private fun waitingProject(tasks: List<Task> = emptyList()) =
        Project(id = 1, name = "Proj", status = ProjectStatus.WAITING, tasks = tasks)

    private fun activeProject(tasks: List<Task> = emptyList()) =
        Project(id = 1, name = "Proj", status = ProjectStatus.IN_PROGRESS, tasks = tasks)

    private fun projects() = InMemoryProjectRepository.projects.value
    private fun project() = projects().first()

    @Before fun setUp() {
        InMemoryProjectRepository.reset()
    }

    // ── startProject ──────────────────────────────────────────────────────────

    @Test fun `startProject transitions WAITING to IN_PROGRESS`() {
        InMemoryProjectRepository.reset(listOf(waitingProject()))
        InMemoryProjectRepository.startProject(1)
        assertEquals(ProjectStatus.IN_PROGRESS, project().status)
    }

    @Test fun `startProject is no-op for already IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject()))
        InMemoryProjectRepository.startProject(1)
        assertEquals(ProjectStatus.IN_PROGRESS, project().status)
    }

    @Test fun `startProject is no-op for DONE project`() {
        val done = Project(id = 1, name = "P", status = ProjectStatus.DONE)
        InMemoryProjectRepository.reset(listOf(done))
        InMemoryProjectRepository.startProject(1)
        assertEquals(ProjectStatus.DONE, project().status)
    }

    // ── finishProject ─────────────────────────────────────────────────────────

    @Test fun `finishProject transitions IN_PROGRESS to DONE when canFinish`() {
        InMemoryProjectRepository.reset(listOf(activeProject(tasks = listOf(doneTask(1)))))
        InMemoryProjectRepository.finishProject(1)
        assertEquals(ProjectStatus.DONE, project().status)
    }

    @Test fun `finishProject sets completedAt timestamp`() {
        val before = System.currentTimeMillis()
        InMemoryProjectRepository.reset(listOf(activeProject(tasks = listOf(doneTask(1)))))
        InMemoryProjectRepository.finishProject(1)
        val completedAt = project().completedAt
        assertTrue(completedAt != null && completedAt >= before)
    }

    @Test fun `finishProject is no-op when project has pending tasks`() {
        InMemoryProjectRepository.reset(listOf(activeProject(tasks = listOf(pendingTask(1)))))
        InMemoryProjectRepository.finishProject(1)
        assertEquals(ProjectStatus.IN_PROGRESS, project().status)
        assertNull(project().completedAt)
    }

    @Test fun `finishProject is no-op for WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject(tasks = listOf(doneTask(1)))))
        InMemoryProjectRepository.finishProject(1)
        assertEquals(ProjectStatus.WAITING, project().status)
    }

    // ── cancelProject ─────────────────────────────────────────────────────────

    @Test fun `cancelProject transitions IN_PROGRESS to CANCELLED`() {
        InMemoryProjectRepository.reset(listOf(activeProject()))
        InMemoryProjectRepository.cancelProject(1)
        assertEquals(ProjectStatus.CANCELLED, project().status)
    }

    @Test fun `cancelProject transitions WAITING to CANCELLED`() {
        InMemoryProjectRepository.reset(listOf(waitingProject()))
        InMemoryProjectRepository.cancelProject(1)
        assertEquals(ProjectStatus.CANCELLED, project().status)
    }

    @Test fun `cancelProject is no-op for DONE project`() {
        val done = Project(id = 1, name = "P", status = ProjectStatus.DONE)
        InMemoryProjectRepository.reset(listOf(done))
        InMemoryProjectRepository.cancelProject(1)
        assertEquals(ProjectStatus.DONE, project().status)
    }

    // ── deleteProject ─────────────────────────────────────────────────────────

    @Test fun `deleteProject removes WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject()))
        InMemoryProjectRepository.deleteProject(1)
        assertTrue(projects().isEmpty())
    }

    @Test fun `deleteProject removes DONE project`() {
        val done = Project(id = 1, name = "P", status = ProjectStatus.DONE)
        InMemoryProjectRepository.reset(listOf(done))
        InMemoryProjectRepository.deleteProject(1)
        assertTrue(projects().isEmpty())
    }

    @Test fun `deleteProject is no-op for IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject()))
        InMemoryProjectRepository.deleteProject(1)
        assertEquals(1, projects().size)
    }

    // ── addTask ───────────────────────────────────────────────────────────────

    @Test fun `addTask adds task to IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject()))
        InMemoryProjectRepository.addTask(1, "Nova task", Priority.HIGH)
        assertEquals(1, project().tasks.size)
        assertEquals("Nova task", project().tasks.first().title)
        assertEquals(Priority.HIGH, project().tasks.first().priority)
    }

    @Test fun `addTask is no-op for WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject()))
        InMemoryProjectRepository.addTask(1, "Nova task", Priority.HIGH)
        assertTrue(project().tasks.isEmpty())
    }

    @Test fun `addTask assigns incremental IDs across projects`() {
        val p2 = Project(id = 2, name = "P2", status = ProjectStatus.IN_PROGRESS,
            tasks = listOf(Task(10, "existing", TaskStatus.PENDING, Priority.LOW)))
        InMemoryProjectRepository.reset(listOf(activeProject(), p2))
        InMemoryProjectRepository.addTask(1, "New", Priority.LOW)
        assertEquals(11, project().tasks.first().id)
    }

    // ── completeTasks ─────────────────────────────────────────────────────────

    @Test fun `completeTasks marks PENDING tasks as DONE in IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(pendingTask(1), pendingTask(2)))))
        InMemoryProjectRepository.completeTasks(1, setOf(1))
        val tasks = project().tasks
        assertEquals(TaskStatus.DONE, tasks.find { it.id == 1 }!!.status)
        assertEquals(TaskStatus.PENDING, tasks.find { it.id == 2 }!!.status)
    }

    @Test fun `completeTasks sets completedAt`() {
        val before = System.currentTimeMillis()
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(pendingTask(1)))))
        InMemoryProjectRepository.completeTasks(1, setOf(1))
        val completedAt = project().tasks.first().completedAt
        assertTrue(completedAt != null && completedAt >= before)
    }

    @Test fun `completeTasks is no-op for WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject(listOf(pendingTask(1)))))
        InMemoryProjectRepository.completeTasks(1, setOf(1))
        assertEquals(TaskStatus.PENDING, project().tasks.first().status)
    }

    @Test fun `completeTasks does not affect already DONE tasks`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(doneTask(1)))))
        val completedAtBefore = project().tasks.first().completedAt
        InMemoryProjectRepository.completeTasks(1, setOf(1))
        assertEquals(completedAtBefore, project().tasks.first().completedAt)
    }

    // ── cancelTasks ───────────────────────────────────────────────────────────

    @Test fun `cancelTasks marks PENDING tasks as CANCELLED in IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(pendingTask(1), pendingTask(2)))))
        InMemoryProjectRepository.cancelTasks(1, setOf(2))
        val tasks = project().tasks
        assertEquals(TaskStatus.PENDING, tasks.find { it.id == 1 }!!.status)
        assertEquals(TaskStatus.CANCELLED, tasks.find { it.id == 2 }!!.status)
    }

    @Test fun `cancelTasks is no-op for WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject(listOf(pendingTask(1)))))
        InMemoryProjectRepository.cancelTasks(1, setOf(1))
        assertEquals(TaskStatus.PENDING, project().tasks.first().status)
    }

    @Test fun `cancelTasks does not affect DONE tasks`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(doneTask(1)))))
        InMemoryProjectRepository.cancelTasks(1, setOf(1))
        assertEquals(TaskStatus.DONE, project().tasks.first().status)
    }

    // ── deleteTasks ───────────────────────────────────────────────────────────

    @Test fun `deleteTasks removes specified tasks regardless of status`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(pendingTask(1), doneTask(2)))))
        InMemoryProjectRepository.deleteTasks(1, setOf(1, 2))
        assertTrue(project().tasks.isEmpty())
    }

    // ── markAllTasksDone ──────────────────────────────────────────────────────

    @Test fun `markAllTasksDone completes all PENDING tasks in IN_PROGRESS project`() {
        InMemoryProjectRepository.reset(listOf(activeProject(listOf(pendingTask(1), pendingTask(2)))))
        InMemoryProjectRepository.markAllTasksDone(1)
        assertTrue(project().tasks.all { it.done })
    }

    @Test fun `markAllTasksDone does not change already DONE or CANCELLED tasks`() {
        val tasks = listOf(doneTask(1), Task(2, "t", TaskStatus.CANCELLED, Priority.LOW))
        InMemoryProjectRepository.reset(listOf(activeProject(tasks)))
        val completedAtBefore = project().tasks.first().completedAt
        InMemoryProjectRepository.markAllTasksDone(1)
        assertEquals(completedAtBefore, project().tasks.first().completedAt)
        assertEquals(TaskStatus.CANCELLED, project().tasks.last().status)
    }

    @Test fun `markAllTasksDone is no-op for WAITING project`() {
        InMemoryProjectRepository.reset(listOf(waitingProject(listOf(pendingTask(1)))))
        InMemoryProjectRepository.markAllTasksDone(1)
        assertEquals(TaskStatus.PENDING, project().tasks.first().status)
    }

    // ── setSortOrder ──────────────────────────────────────────────────────────

    @Test fun `setSortOrder updates the sort order flow`() {
        InMemoryProjectRepository.setSortOrder(SortOrder.NEWEST)
        assertEquals(SortOrder.NEWEST, InMemoryProjectRepository.sortOrder.value)
    }

    @Test fun `reset restores default sort order`() {
        InMemoryProjectRepository.setSortOrder(SortOrder.OLDEST)
        InMemoryProjectRepository.reset()
        assertEquals(SortOrder.NAME_ASC, InMemoryProjectRepository.sortOrder.value)
    }
}

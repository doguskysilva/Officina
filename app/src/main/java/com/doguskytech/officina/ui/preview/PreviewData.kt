package com.doguskytech.officina.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.Task
import com.doguskytech.officina.domain.model.TaskStatus
import com.doguskytech.officina.ui.UiState

private val NOW = System.currentTimeMillis()
private fun daysAgo(days: Int): Long = NOW - days * 86_400_000L

// ── Dados fake ────────────────────────────────────────────────────────────────

val fakeProjects = listOf(
    Project(
        id = 1, name = "App Mobile",
        status = ProjectStatus.IN_PROGRESS,
        createdAt = daysAgo(30),
        tasks = listOf(
            Task(1, "Tela de login",       TaskStatus.DONE,    Priority.HIGH,   daysAgo(29), daysAgo(27)),
            Task(2, "Tela home",           TaskStatus.DONE,    Priority.HIGH,   daysAgo(28), daysAgo(25)),
            Task(3, "Integração OAuth",    TaskStatus.PENDING, Priority.HIGH,   daysAgo(26)),
            Task(4, "Push notifications",  TaskStatus.PENDING, Priority.MEDIUM, daysAgo(24)),
            Task(5, "Deep links",          TaskStatus.PENDING, Priority.LOW,    daysAgo(24)),
        )
    ),
    Project(
        id = 2, name = "API Backend",
        status = ProjectStatus.IN_PROGRESS,
        createdAt = daysAgo(20),
        tasks = listOf(
            Task(6,  "Endpoints de auth",    TaskStatus.DONE,    Priority.HIGH,   daysAgo(19), daysAgo(15)),
            Task(7,  "CRUD de projetos",     TaskStatus.PENDING, Priority.HIGH,   daysAgo(18)),
            Task(8,  "Rate limiting",        TaskStatus.PENDING, Priority.MEDIUM, daysAgo(17)),
            Task(9,  "Documentação OpenAPI", TaskStatus.PENDING, Priority.LOW,    daysAgo(16)),
        )
    ),
    Project(
        id = 3, name = "Design System",
        status = ProjectStatus.DONE,
        createdAt = daysAgo(60),
        completedAt = daysAgo(5),
        tasks = listOf(
            Task(10, "Tokens de cor",        TaskStatus.DONE, Priority.HIGH,   daysAgo(59), daysAgo(20)),
            Task(11, "Componente Button",    TaskStatus.DONE, Priority.HIGH,   daysAgo(58), daysAgo(18)),
            Task(12, "Componente Card",      TaskStatus.DONE, Priority.MEDIUM, daysAgo(55), daysAgo(15)),
            Task(13, "Componente TextField", TaskStatus.DONE, Priority.MEDIUM, daysAgo(50), daysAgo(10)),
            Task(14, "Dark mode",            TaskStatus.DONE, Priority.LOW,    daysAgo(45), daysAgo(6)),
        )
    ),
    Project(
        id = 4, name = "Documentação",
        status = ProjectStatus.WAITING,
        createdAt = daysAgo(2),
        tasks = listOf(
            Task(15, "README geral",         TaskStatus.PENDING, Priority.MEDIUM, daysAgo(1)),
            Task(16, "Guia de contribuição", TaskStatus.PENDING, Priority.LOW,    daysAgo(1)),
        )
    ),
)

val fakeProjectAllDone = Project(
    id = 3, name = "Design System",
    status = ProjectStatus.DONE,
    createdAt = daysAgo(60),
    completedAt = daysAgo(5),
    tasks = listOf(
        Task(10, "Tokens de cor",     TaskStatus.DONE, Priority.HIGH,   daysAgo(59), daysAgo(20)),
        Task(11, "Componente Button", TaskStatus.DONE, Priority.HIGH,   daysAgo(58), daysAgo(18)),
        Task(12, "Componente Card",   TaskStatus.DONE, Priority.MEDIUM, daysAgo(55), daysAgo(15)),
    )
)

val fakeProjectWaiting = Project(
    id = 4, name = "Documentação",
    status = ProjectStatus.WAITING,
    createdAt = daysAgo(2),
    tasks = listOf(
        Task(15, "README geral",         TaskStatus.PENDING, Priority.MEDIUM, daysAgo(1)),
        Task(16, "Guia de contribuição", TaskStatus.PENDING, Priority.LOW,    daysAgo(1)),
    )
)

val fakeTasks = fakeProjects.flatMap { project ->
    project.tasks.map { task ->
        TaskWithProject(task = task, projectId = project.id, projectName = project.name)
    }
}

// ── ProjectList ───────────────────────────────────────────────────────────────

class ProjectListStateProvider : PreviewParameterProvider<UiState<List<Project>>> {
    override val values = sequenceOf(
        UiState.Loading,
        UiState.Success(emptyList()),
        UiState.Success(fakeProjects),
        UiState.Error("Falha ao carregar projetos"),
    )
}

// ── ProjectDetail ─────────────────────────────────────────────────────────────

class ProjectDetailStateProvider : PreviewParameterProvider<UiState<Project>> {
    override val values = sequenceOf(
        UiState.Loading,
        UiState.Success(fakeProjects.first()),
        UiState.Success(fakeProjectAllDone),
        UiState.Success(fakeProjectWaiting),
        UiState.Error("Projeto não encontrado"),
    )
}

// ── TaskList ──────────────────────────────────────────────────────────────────

class TaskListStateProvider : PreviewParameterProvider<UiState<List<TaskWithProject>>> {
    override val values = sequenceOf(
        UiState.Loading,
        UiState.Success(emptyList()),
        UiState.Success(fakeTasks),
        UiState.Error("Falha ao carregar tarefas"),
    )
}

package com.doguskytech.officina.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.doguskytech.officina.data.Priority
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.data.Task
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.ui.UiState

// ── Dados fake ────────────────────────────────────────────────────────────────

val fakeProjects = listOf(
    Project(
        id = 1, name = "App Mobile",
        tasks = listOf(
            Task(1, "Tela de login",       done = true, priority = Priority.HIGH),
            Task(2, "Tela home",           done = true, priority = Priority.HIGH),
            Task(3, "Integração OAuth",                 priority = Priority.HIGH),
            Task(4, "Push notifications",               priority = Priority.MEDIUM),
            Task(5, "Deep links",                       priority = Priority.LOW),
        )
    ),
    Project(
        id = 2, name = "API Backend",
        tasks = listOf(
            Task(6, "Endpoints de auth",  done = true, priority = Priority.HIGH),
            Task(7, "CRUD de projetos",                priority = Priority.HIGH),
            Task(8, "Rate limiting",                   priority = Priority.MEDIUM),
            Task(9, "Documentação OpenAPI",            priority = Priority.LOW),
        )
    ),
    Project(
        id = 3, name = "Design System",
        tasks = listOf(
            Task(10, "Tokens de cor",       done = true, priority = Priority.HIGH),
            Task(11, "Componente Button",   done = true, priority = Priority.HIGH),
            Task(12, "Componente Card",     done = true, priority = Priority.MEDIUM),
            Task(13, "Componente TextField",            priority = Priority.MEDIUM),
            Task(14, "Dark mode",                       priority = Priority.LOW),
        )
    ),
    Project(
        id = 4, name = "Documentação",
        tasks = listOf(
            Task(15, "README geral",                    priority = Priority.MEDIUM),
            Task(16, "Guia de contribuição",            priority = Priority.LOW),
        )
    ),
)

val fakeProjectAllDone = Project(
    id = 3, name = "Design System",
    tasks = listOf(
        Task(10, "Tokens de cor",     done = true, priority = Priority.HIGH),
        Task(11, "Componente Button", done = true, priority = Priority.HIGH),
        Task(12, "Componente Card",   done = true, priority = Priority.MEDIUM),
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
        UiState.Success(fakeProjects.first()),           // tarefas mistas
        UiState.Success(fakeProjectAllDone),             // tudo concluído
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

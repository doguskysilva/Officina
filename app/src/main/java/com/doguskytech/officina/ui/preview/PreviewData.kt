package com.doguskytech.officina.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.data.Task
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.ui.UiState

// ── Dados fake ────────────────────────────────────────────────────────────────

val fakeProjects = listOf(
    Project(
        id = 1, name = "App Mobile",
        tasks = listOf(
            Task(1, "Tela de login", done = true),
            Task(2, "Tela home", done = true),
            Task(3, "Integração OAuth"),
        )
    ),
    Project(
        id = 2, name = "API Backend",
        tasks = listOf(
            Task(4, "Endpoints de auth", done = true),
            Task(5, "CRUD de projetos"),
        )
    ),
    Project(
        id = 3, name = "Design System",
        tasks = listOf(
            Task(6, "Tokens de cor", done = true),
            Task(7, "Componente Button", done = true),
            Task(8, "Componente Card", done = true),
        )
    ),
    Project(
        id = 4, name = "Documentação",
        tasks = listOf(
            Task(9, "README geral"),
            Task(10, "Guia de contribuição"),
        )
    ),
)

val fakeProjectAllDone = Project(
    id = 3, name = "Design System",
    tasks = listOf(
        Task(6, "Tokens de cor", done = true),
        Task(7, "Componente Button", done = true),
        Task(8, "Componente Card", done = true),
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

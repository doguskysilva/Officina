package com.doguskytech.officina.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ProjectRepository {

    private val _projects = MutableStateFlow(
        listOf(
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
    )

    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    fun addTask(projectId: Int, title: String) {
        _projects.update { current ->
            val nextId = (current.flatMap { it.tasks }.maxOfOrNull { it.id } ?: 0) + 1
            current.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks + Task(nextId, title))
                else project
            }
        }
    }

    fun toggleTask(projectId: Int, taskId: Int) {
        _projects.update { current ->
            current.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks.map { task ->
                        if (task.id == taskId) task.copy(done = !task.done) else task
                    })
                else project
            }
        }
    }

    fun deleteProject(id: Int) {
        _projects.update { current -> current.filter { it.id != id } }
    }

    fun markAllTasksDone(projectId: Int) {
        _projects.update { projects ->
            projects.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks.map { it.copy(done = true) })
                else project
            }
        }
    }
}

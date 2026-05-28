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
                    Task(1,  "Tela de login",        done = true,  priority = Priority.HIGH),
                    Task(2,  "Tela home",             done = true,  priority = Priority.HIGH),
                    Task(3,  "Integração OAuth",                    priority = Priority.HIGH),
                    Task(4,  "Push notifications",                  priority = Priority.MEDIUM),
                    Task(5,  "Deep links",                          priority = Priority.LOW),
                )
            ),
            Project(
                id = 2, name = "API Backend",
                tasks = listOf(
                    Task(6,  "Endpoints de auth",    done = true,  priority = Priority.HIGH),
                    Task(7,  "CRUD de projetos",                    priority = Priority.HIGH),
                    Task(8,  "Rate limiting",                       priority = Priority.MEDIUM),
                    Task(9,  "Documentação OpenAPI",                priority = Priority.LOW),
                )
            ),
            Project(
                id = 3, name = "Design System",
                tasks = listOf(
                    Task(10, "Tokens de cor",        done = true,  priority = Priority.HIGH),
                    Task(11, "Componente Button",    done = true,  priority = Priority.HIGH),
                    Task(12, "Componente Card",      done = true,  priority = Priority.MEDIUM),
                    Task(13, "Componente TextField",               priority = Priority.MEDIUM),
                    Task(14, "Dark mode",                          priority = Priority.LOW),
                )
            ),
            Project(
                id = 4, name = "Documentação",
                tasks = listOf(
                    Task(15, "README geral",                        priority = Priority.MEDIUM),
                    Task(16, "Guia de contribuição",                priority = Priority.LOW),
                )
            ),
            Project(
                id = 5, name = "Plataforma de E-commerce",
                tasks = listOf(
                    // High priority — done
                    Task(17, "Autenticação e cadastro",  done = true,  priority = Priority.HIGH),
                    Task(18, "Listagem de produtos",     done = true,  priority = Priority.HIGH),
                    Task(19, "Carrinho de compras",      done = true,  priority = Priority.HIGH),
                    Task(20, "Checkout e pagamento",     done = true,  priority = Priority.HIGH),
                    // High priority — pendente
                    Task(21, "Integração com gateway",               priority = Priority.HIGH),
                    Task(22, "Notificação de pedido",                 priority = Priority.HIGH),
                    // Medium — done
                    Task(23, "Filtro por categoria",    done = true,  priority = Priority.MEDIUM),
                    Task(24, "Busca de produtos",       done = true,  priority = Priority.MEDIUM),
                    // Medium — pendente
                    Task(25, "Avaliação de produtos",                 priority = Priority.MEDIUM),
                    Task(26, "Wishlist do usuário",                   priority = Priority.MEDIUM),
                    Task(27, "Histórico de pedidos",                  priority = Priority.MEDIUM),
                    // Low — done
                    Task(28, "SEO e meta tags",         done = true,  priority = Priority.LOW),
                    // Low — pendente
                    Task(29, "Newsletter",                            priority = Priority.LOW),
                    Task(30, "Chat de suporte",                       priority = Priority.LOW),
                )
            ),
        )
    )

    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    fun addTask(projectId: Int, title: String, priority: Priority = Priority.MEDIUM) {
        _projects.update { current ->
            val nextId = (current.flatMap { it.tasks }.maxOfOrNull { it.id } ?: 0) + 1
            current.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks + Task(nextId, title, priority = priority))
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

    fun completeTasks(projectId: Int, taskIds: Set<Int>) {
        _projects.update { current ->
            current.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks.map { task ->
                        if (task.id in taskIds) task.copy(done = true) else task
                    })
                else project
            }
        }
    }

    fun deleteTasks(projectId: Int, taskIds: Set<Int>) {
        _projects.update { current ->
            current.map { project ->
                if (project.id == projectId)
                    project.copy(tasks = project.tasks.filter { it.id !in taskIds })
                else project
            }
        }
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

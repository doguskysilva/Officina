package com.doguskytech.officina.data

import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.SortOrder
import com.doguskytech.officina.domain.model.Task
import com.doguskytech.officina.domain.model.TaskStatus
import com.doguskytech.officina.domain.repository.ProjectRepository
import com.doguskytech.officina.domain.rules.ProjectRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object InMemoryProjectRepository : ProjectRepository {

    private val NOW = System.currentTimeMillis()
    private fun daysAgo(days: Int): Long = NOW - days * 86_400_000L

    private val _projects = MutableStateFlow(
        listOf(
            Project(
                id = 1, name = "App Mobile",
                status = ProjectStatus.IN_PROGRESS,
                createdAt = daysAgo(30),
                tasks = listOf(
                    Task(1,  "Tela de login",       TaskStatus.DONE,    Priority.HIGH,   daysAgo(29), daysAgo(27)),
                    Task(2,  "Tela home",            TaskStatus.DONE,    Priority.HIGH,   daysAgo(28), daysAgo(25)),
                    Task(3,  "Integração OAuth",     TaskStatus.PENDING, Priority.HIGH,   daysAgo(26)),
                    Task(4,  "Push notifications",   TaskStatus.PENDING, Priority.MEDIUM, daysAgo(24)),
                    Task(5,  "Deep links",           TaskStatus.PENDING, Priority.LOW,    daysAgo(24)),
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
                    Task(15, "README geral",          TaskStatus.PENDING, Priority.MEDIUM, daysAgo(1)),
                    Task(16, "Guia de contribuição",  TaskStatus.PENDING, Priority.LOW,    daysAgo(1)),
                )
            ),
            Project(
                id = 5, name = "Plataforma de E-commerce",
                status = ProjectStatus.CANCELLED,
                createdAt = daysAgo(45),
                tasks = listOf(
                    Task(17, "Autenticação e cadastro",  TaskStatus.DONE,      Priority.HIGH,   daysAgo(44), daysAgo(40)),
                    Task(18, "Listagem de produtos",     TaskStatus.DONE,      Priority.HIGH,   daysAgo(44), daysAgo(38)),
                    Task(19, "Carrinho de compras",      TaskStatus.DONE,      Priority.HIGH,   daysAgo(43), daysAgo(35)),
                    Task(20, "Checkout e pagamento",     TaskStatus.DONE,      Priority.HIGH,   daysAgo(42), daysAgo(32)),
                    Task(21, "Integração com gateway",   TaskStatus.CANCELLED, Priority.HIGH,   daysAgo(40)),
                    Task(22, "Notificação de pedido",    TaskStatus.CANCELLED, Priority.HIGH,   daysAgo(40)),
                    Task(23, "Filtro por categoria",     TaskStatus.DONE,      Priority.MEDIUM, daysAgo(43), daysAgo(35)),
                    Task(24, "Busca de produtos",        TaskStatus.DONE,      Priority.MEDIUM, daysAgo(43), daysAgo(33)),
                    Task(25, "Avaliação de produtos",    TaskStatus.CANCELLED, Priority.MEDIUM, daysAgo(38)),
                    Task(26, "Wishlist do usuário",      TaskStatus.CANCELLED, Priority.MEDIUM, daysAgo(38)),
                    Task(27, "Histórico de pedidos",     TaskStatus.CANCELLED, Priority.MEDIUM, daysAgo(37)),
                    Task(28, "SEO e meta tags",          TaskStatus.DONE,      Priority.LOW,    daysAgo(40), daysAgo(30)),
                    Task(29, "Newsletter",               TaskStatus.CANCELLED, Priority.LOW,    daysAgo(37)),
                    Task(30, "Chat de suporte",          TaskStatus.CANCELLED, Priority.LOW,    daysAgo(37)),
                )
            ),
        )
    )

    override val projects = _projects.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    override val sortOrder = _sortOrder.asStateFlow()

    private fun now() = System.currentTimeMillis()

    private fun updateProject(projectId: Int, transform: (Project) -> Project) {
        _projects.update { current -> current.map { if (it.id == projectId) transform(it) else it } }
    }

    override fun addProject(name: String) {
        _projects.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current + Project(id = nextId, name = name)
        }
    }

    override fun addTask(projectId: Int, title: String, priority: Priority) {
        _projects.update { current ->
            val project = current.find { it.id == projectId } ?: return@update current
            if (!ProjectRules.canAddTask(project)) return@update current
            val nextId = (current.flatMap { it.tasks }.maxOfOrNull { it.id } ?: 0) + 1
            current.map { p ->
                if (p.id == projectId) p.copy(tasks = p.tasks + Task(nextId, title, priority = priority))
                else p
            }
        }
    }

    override fun completeTasks(projectId: Int, taskIds: Set<Int>) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canCompleteTasks(project)) return@updateProject project
            project.copy(tasks = project.tasks.map { task ->
                if (task.id in taskIds && task.isPending)
                    task.copy(status = TaskStatus.DONE, completedAt = now())
                else task
            })
        }
    }

    override fun cancelTasks(projectId: Int, taskIds: Set<Int>) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canCancelTasks(project)) return@updateProject project
            project.copy(tasks = project.tasks.map { task ->
                if (task.id in taskIds && task.isPending)
                    task.copy(status = TaskStatus.CANCELLED)
                else task
            })
        }
    }

    override fun deleteTasks(projectId: Int, taskIds: Set<Int>) {
        updateProject(projectId) { project ->
            project.copy(tasks = project.tasks.filter { it.id !in taskIds })
        }
    }

    override fun markAllTasksDone(projectId: Int) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canCompleteTasks(project)) return@updateProject project
            project.copy(tasks = project.tasks.map { task ->
                if (task.isPending) task.copy(status = TaskStatus.DONE, completedAt = now())
                else task
            })
        }
    }

    override fun startProject(projectId: Int) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canStart(project)) return@updateProject project
            project.copy(status = ProjectStatus.IN_PROGRESS)
        }
    }

    override fun finishProject(projectId: Int) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canFinish(project)) return@updateProject project
            project.copy(status = ProjectStatus.DONE, completedAt = now())
        }
    }

    override fun cancelProject(projectId: Int) {
        updateProject(projectId) { project ->
            if (!ProjectRules.canCancel(project)) return@updateProject project
            project.copy(status = ProjectStatus.CANCELLED)
        }
    }

    override fun deleteProject(projectId: Int) {
        _projects.update { current ->
            val project = current.find { it.id == projectId } ?: return@update current
            if (!ProjectRules.canDelete(project)) return@update current
            current.filter { it.id != projectId }
        }
    }

    override fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    internal fun reset(initialProjects: List<Project> = emptyList()) {
        _projects.value = initialProjects
        _sortOrder.value = SortOrder.NAME_ASC
    }
}

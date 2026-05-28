package com.doguskytech.officina.data

import com.doguskytech.officina.domain.repository.ProjectRepository as DomainProjectRepository

// Backward-compat wrapper — widgets and legacy callers use this name.
// All real logic lives in InMemoryProjectRepository.
object ProjectRepository : DomainProjectRepository by InMemoryProjectRepository {
    fun toggleTask(projectId: Int, taskId: Int) =
        InMemoryProjectRepository.completeTasks(projectId, setOf(taskId))
}

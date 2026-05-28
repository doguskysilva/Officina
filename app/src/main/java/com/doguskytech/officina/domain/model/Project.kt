package com.doguskytech.officina.domain.model

data class Project(
    val id: Int,
    val name: String,
    val tasks: List<Task> = emptyList(),
    val status: ProjectStatus = ProjectStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
) {
    val pendingCount: Int get() = tasks.count { it.isPending }
    val canFinish: Boolean get() = tasks.isNotEmpty() && pendingCount == 0
    val isActive: Boolean get() = status == ProjectStatus.IN_PROGRESS
}

package com.doguskytech.officina.domain.model

data class Task(
    val id: Int,
    val title: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
) {
    val done: Boolean get() = status == TaskStatus.DONE
    val isPending: Boolean get() = status == TaskStatus.PENDING
}

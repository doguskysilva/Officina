package com.doguskytech.officina.data

data class TaskWithProject(
    val task: Task,
    val projectId: Int,
    val projectName: String,
)

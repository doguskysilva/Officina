package com.doguskytech.officina.data

data class Project(
    val id: Int,
    val name: String,
    val tasks: List<Task> = emptyList(),
)

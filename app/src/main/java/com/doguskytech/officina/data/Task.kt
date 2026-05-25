package com.doguskytech.officina.data

data class Task(
    val id: Int,
    val title: String,
    val done: Boolean = false,
)

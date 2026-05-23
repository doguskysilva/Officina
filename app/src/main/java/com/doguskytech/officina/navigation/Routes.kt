package com.doguskytech.officina.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// data object → rota sem argumentos
@Serializable
data object ProjectList : NavKey

// data class → rota com argumentos (type-safe, sem strings)
@Serializable
data class ProjectDetail(val projectId: Int, val projectName: String) : NavKey

@Serializable
data class NewTask(val projectId: Int) : NavKey

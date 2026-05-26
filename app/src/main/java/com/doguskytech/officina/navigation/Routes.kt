package com.doguskytech.officina.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// data object → rota sem argumentos
@Serializable
data object ProjectList : NavKey

// data class → rota com argumentos (type-safe, sem strings)
@Serializable
data class ProjectDetail(
    val projectId: Int,
    val projectName: String,
    val highlightTaskId: Int? = null,
) : NavKey

@Serializable
data class NewTask(val projectId: Int) : NavKey

// Diálogo de confirmação — abrirá como Dialog via DialogSceneStrategy
@Serializable
data class ConfirmDelete(val projectId: Int, val projectName: String) : NavKey

// Rotas top-level (abas de navegação)
@Serializable data object TaskList : NavKey

@Serializable data object AppSettings : NavKey

// Overlay — abre como BottomSheet via BottomSheetSceneStrategy
@Serializable data object SortProjects : NavKey

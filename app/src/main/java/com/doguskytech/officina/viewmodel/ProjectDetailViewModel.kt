package com.doguskytech.officina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.data.ProjectRepository
import com.doguskytech.officina.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProjectDetailViewModel(private val projectId: Int) : ViewModel() {

    val uiState: StateFlow<UiState<Project>> = ProjectRepository.projects
        .map<List<Project>, UiState<Project>> { projects ->
            val project = projects.find { it.id == projectId }
            if (project != null) UiState.Success(project)
            else UiState.Error("Projeto não encontrado")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun deleteProject() = ProjectRepository.deleteProject(projectId)
}

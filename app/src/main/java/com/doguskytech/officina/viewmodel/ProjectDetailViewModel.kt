package com.doguskytech.officina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doguskytech.officina.data.InMemoryProjectRepository
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProjectDetailViewModel(private val projectId: Int) : ViewModel() {

    val uiState: StateFlow<UiState<Project>> = InMemoryProjectRepository.projects
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

    fun completeTasks(taskIds: Set<Int>) = InMemoryProjectRepository.completeTasks(projectId, taskIds)
    fun cancelTasks(taskIds: Set<Int>)   = InMemoryProjectRepository.cancelTasks(projectId, taskIds)
    fun deleteTasks(taskIds: Set<Int>)   = InMemoryProjectRepository.deleteTasks(projectId, taskIds)
    fun markAllTasksDone()               = InMemoryProjectRepository.markAllTasksDone(projectId)
    fun startProject()                   = InMemoryProjectRepository.startProject(projectId)
    fun finishProject()                  = InMemoryProjectRepository.finishProject(projectId)
    fun cancelProject()                  = InMemoryProjectRepository.cancelProject(projectId)
    fun deleteProject()                  = InMemoryProjectRepository.deleteProject(projectId)
}

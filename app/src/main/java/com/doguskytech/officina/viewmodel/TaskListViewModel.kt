package com.doguskytech.officina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doguskytech.officina.data.InMemoryProjectRepository
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TaskListViewModel : ViewModel() {

    val uiState: StateFlow<UiState<List<TaskWithProject>>> = InMemoryProjectRepository.projects
        .map<List<Project>, UiState<List<TaskWithProject>>> { projects ->
            UiState.Success(
                projects.flatMap { project ->
                    project.tasks.map { task ->
                        TaskWithProject(task, project.id, project.name)
                    }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
}

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

class ProjectListViewModel : ViewModel() {

    val uiState: StateFlow<UiState<List<Project>>> = ProjectRepository.projects
        .map<List<Project>, UiState<List<Project>>> { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )
}

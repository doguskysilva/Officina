package com.doguskytech.officina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doguskytech.officina.data.InMemoryProjectRepository
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.SortOrder
import com.doguskytech.officina.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProjectListViewModel : ViewModel() {

    val uiState: StateFlow<UiState<List<Project>>> =
        combine(InMemoryProjectRepository.projects, InMemoryProjectRepository.sortOrder) { projects, sort ->
            UiState.Success(
                when (sort) {
                    SortOrder.NAME_ASC -> projects.sortedBy { it.name.lowercase() }
                    SortOrder.NEWEST   -> projects.sortedByDescending { it.createdAt }
                    SortOrder.OLDEST   -> projects.sortedBy { it.createdAt }
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )
}

package com.doguskytech.officina.viewmodel

import androidx.lifecycle.ViewModel
import com.doguskytech.officina.data.InMemoryProjectRepository
import com.doguskytech.officina.domain.model.SortOrder
import kotlinx.coroutines.flow.StateFlow

class SortProjectsViewModel : ViewModel() {
    val currentSort: StateFlow<SortOrder> = InMemoryProjectRepository.sortOrder
    fun setSort(order: SortOrder) = InMemoryProjectRepository.setSortOrder(order)
}

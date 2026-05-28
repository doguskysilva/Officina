package com.doguskytech.officina.domain.repository

import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.Project
import com.doguskytech.officina.domain.model.SortOrder
import kotlinx.coroutines.flow.StateFlow

interface ProjectRepository {
    val projects: StateFlow<List<Project>>
    val sortOrder: StateFlow<SortOrder>
    fun addProject(name: String)
    fun deleteProject(projectId: Int)
    fun addTask(projectId: Int, title: String, priority: Priority)
    fun completeTasks(projectId: Int, taskIds: Set<Int>)
    fun cancelTasks(projectId: Int, taskIds: Set<Int>)
    fun deleteTasks(projectId: Int, taskIds: Set<Int>)
    fun markAllTasksDone(projectId: Int)
    fun startProject(projectId: Int)
    fun finishProject(projectId: Int)
    fun cancelProject(projectId: Int)
    fun setSortOrder(order: SortOrder)
}

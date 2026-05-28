package com.doguskytech.officina.ui

import androidx.annotation.StringRes
import com.doguskytech.officina.R
import com.doguskytech.officina.domain.model.Priority
import com.doguskytech.officina.domain.model.ProjectStatus
import com.doguskytech.officina.domain.model.TaskStatus

val Priority.labelRes: Int
    @StringRes get() = when (this) {
        Priority.LOW    -> R.string.priority_low
        Priority.MEDIUM -> R.string.priority_medium
        Priority.HIGH   -> R.string.priority_high
    }

val TaskStatus.labelRes: Int
    @StringRes get() = when (this) {
        TaskStatus.PENDING   -> R.string.task_status_pending
        TaskStatus.DONE      -> R.string.task_status_done
        TaskStatus.CANCELLED -> R.string.task_status_cancelled
    }

val ProjectStatus.labelRes: Int
    @StringRes get() = when (this) {
        ProjectStatus.WAITING     -> R.string.project_status_waiting
        ProjectStatus.IN_PROGRESS -> R.string.project_status_in_progress
        ProjectStatus.DONE        -> R.string.project_status_done
        ProjectStatus.CANCELLED   -> R.string.project_status_cancelled
    }

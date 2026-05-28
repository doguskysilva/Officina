package com.doguskytech.officina.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.doguskytech.officina.domain.model.Priority

@Composable
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW    -> MaterialTheme.colorScheme.tertiary
    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
    Priority.HIGH   -> MaterialTheme.colorScheme.error
}

package com.doguskytech.officina.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.doguskytech.officina.data.TaskWithProject
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.preview.OfficinaPreviews
import com.doguskytech.officina.ui.preview.TaskListStateProvider
import com.doguskytech.officina.ui.theme.OfficinaTheme

// Todos os estados × phone+tablet × light+dark
@OfficinaPreviews
@Composable
private fun TaskListScreenPreview(
    @PreviewParameter(TaskListStateProvider::class) uiState: UiState<List<TaskWithProject>>,
) {
    OfficinaTheme {
        TaskListScreen(
            uiState = uiState,
            onTaskClick = { _, _, _ -> },
        )
    }
}

package com.doguskytech.officina.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.preview.DevicePreviews
import com.doguskytech.officina.ui.preview.DynamicColorPreviews
import com.doguskytech.officina.ui.preview.OfficinaPreviews
import com.doguskytech.officina.ui.preview.ProjectDetailStateProvider
import com.doguskytech.officina.ui.preview.fakeProjectAllDone
import com.doguskytech.officina.ui.preview.fakeProjects
import com.doguskytech.officina.ui.theme.OfficinaTheme

// Todos os estados × phone+tablet × light+dark
@OfficinaPreviews
@Composable
private fun ProjectDetailScreenPreview(
    @PreviewParameter(ProjectDetailStateProvider::class) uiState: UiState<Project>,
) {
    OfficinaTheme {
        ProjectDetailScreen(
            uiState = uiState,
            onBack = {},
            onNewTaskClick = {},
            onDeleteClick = {},
            onTaskToggle = {},
            onMarkAllDone = {},
        )
    }
}

// Dynamic colors — separado porque não entra no OfficinaPreviews de propósito
@DynamicColorPreviews
@Composable
private fun ProjectDetailScreenDynamicColorsPreview() {
    OfficinaTheme {
        ProjectDetailScreen(
            uiState = UiState.Success(fakeProjects.first()),
            onBack = {},
            onNewTaskClick = {},
            onDeleteClick = {},
            onTaskToggle = {},
            onMarkAllDone = {},
        )
    }
}

// Landscape + foldable desdobrado — formatos que OfficinaPreviews não cobre
@DevicePreviews
@Composable
private fun ProjectDetailScreenDevicesPreview() {
    OfficinaTheme {
        ProjectDetailScreen(
            uiState = UiState.Success(fakeProjectAllDone),
            onBack = {},
            onNewTaskClick = {},
            onDeleteClick = {},
            onTaskToggle = {},
            onMarkAllDone = {},
        )
    }
}

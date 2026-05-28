package com.doguskytech.officina.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.doguskytech.officina.data.Project
import com.doguskytech.officina.ui.UiState
import com.doguskytech.officina.ui.preview.DevicePreviews
import com.doguskytech.officina.ui.preview.FontScalePreviews
import com.doguskytech.officina.ui.preview.OfficinaPreviews
import com.doguskytech.officina.ui.preview.ProjectListStateProvider
import com.doguskytech.officina.ui.preview.fakeProjects
import com.doguskytech.officina.ui.theme.OfficinaTheme

@OfficinaPreviews
@Composable
private fun ProjectListScreenPreview(
    @PreviewParameter(ProjectListStateProvider::class) uiState: UiState<List<Project>>,
) {
    OfficinaTheme {
        ProjectListScreen(
            uiState = uiState,
            selectedProjectId = null,
            onProjectClick = {},
            onSortClick = {},
            onNewProjectClick = {},
        )
    }
}

@OfficinaPreviews
@Composable
private fun ProjectListScreenSelectedPreview() {
    OfficinaTheme {
        ProjectListScreen(
            uiState = UiState.Success(
                fakeProjects
            ),
            selectedProjectId = 1,
            onProjectClick = {},
            onSortClick = {},
            onNewProjectClick = {},
        )
    }
}

@DevicePreviews
@Composable
private fun ProjectListScreenDevicesPreview() {
    OfficinaTheme {
        ProjectListScreen(
            uiState = UiState.Success(
                fakeProjects
            ),
            selectedProjectId = null,
            onProjectClick = {},
            onSortClick = {},
            onNewProjectClick = {},
        )
    }
}

@FontScalePreviews
@Composable
private fun ProjectListScreenFontScalePreview() {
    OfficinaTheme {
        ProjectListScreen(
            uiState = UiState.Success(
                fakeProjects
            ),
            selectedProjectId = null,
            onProjectClick = {},
            onSortClick = {},
            onNewProjectClick = {},
        )
    }
}

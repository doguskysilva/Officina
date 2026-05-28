package com.doguskytech.officina.screens

import androidx.compose.runtime.Composable
import com.doguskytech.officina.ui.preview.DevicePreviews
import com.doguskytech.officina.ui.preview.LightDarkPreviews
import com.doguskytech.officina.ui.theme.OfficinaTheme

@LightDarkPreviews
@Composable
private fun ProjectDetailPlaceholderPreview() {
    OfficinaTheme {
        ProjectDetailPlaceholder()
    }
}

@DevicePreviews
@Composable
private fun SettingsScreenPreview() {
    OfficinaTheme {
        SettingsScreen()
    }
}

@LightDarkPreviews
@Composable
private fun NewTaskScreenPreview() {
    OfficinaTheme {
        NewTaskScreen(
            projectId = 1,
            onBack = {},
            onSave = {},
        )
    }
}

@LightDarkPreviews
@Composable
private fun ConfirmDeleteDialogPreview() {
    OfficinaTheme {
        ConfirmDeleteDialog(
            projectName = "App Mobile",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@LightDarkPreviews
@Composable
private fun SortProjectsSheetPreview() {
    OfficinaTheme {
        SortProjectsSheet()
    }
}

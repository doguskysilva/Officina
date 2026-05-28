package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.doguskytech.officina.R
import com.doguskytech.officina.data.Priority

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewTaskFormContent(
    title: String,
    onTitleChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.task_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            Priority.entries.forEachIndexed { index, p ->
                ToggleButton(
                    checked = p == priority,
                    onCheckedChange = { if (it) onPriorityChange(p) },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        Priority.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { role = Role.RadioButton },
                ) {
                    Text(stringResource(p.labelRes))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewTaskScreen(
    projectId: Int,
    onBack: () -> Unit,
    onSave: (title: String, priority: Priority) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_task_title)) },
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed(block = onBack)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = dropUnlessResumed { if (title.isNotBlank()) onSave(title, priority) },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text(stringResource(R.string.action_save)) },
                expanded = title.isNotBlank(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NewTaskFormContent(
                title = title,
                onTitleChange = { title = it },
                priority = priority,
                onPriorityChange = { priority = it },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewTaskDialog(
    onBack: () -> Unit,
    onSave: (title: String, priority: Priority) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onBack,
        title = { Text(stringResource(R.string.new_task_title)) },
        text = {
            NewTaskFormContent(
                title = title,
                onTitleChange = { title = it },
                priority = priority,
                onPriorityChange = { priority = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, priority) },
                enabled = title.isNotBlank(),
            ) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

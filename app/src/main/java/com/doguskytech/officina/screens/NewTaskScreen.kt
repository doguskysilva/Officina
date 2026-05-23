package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    projectId: Int,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
) {
    // rememberSaveable → sobrevive a rotação de tela (igual ao rememberNavBackStack)
    var taskName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Tarefa") },
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed(block = onBack)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Projeto #$projectId")
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Nome da tarefa") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = dropUnlessResumed { onSave(taskName) },
                enabled = taskName.isNotBlank()
            ) {
                Text("Salvar")
            }
        }
    }
}

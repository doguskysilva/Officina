package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

// Conteúdo do diálogo — um Composable normal, sem saber que está dentro de um Dialog.
// Quem decide que isso vira Dialog é a SceneStrategy no NavDisplay, via metadata.
@Composable
fun ConfirmDeleteDialog(
    projectName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Excluir projeto?",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "\"$projectName\" será removido permanentemente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = dropUnlessResumed(block = onDismiss)) {
                    Text("Cancelar")
                }
                OutlinedButton(
                    onClick = dropUnlessResumed(block = onConfirm),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

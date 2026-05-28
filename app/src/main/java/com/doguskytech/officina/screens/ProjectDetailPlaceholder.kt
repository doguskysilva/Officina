package com.doguskytech.officina.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.doguskytech.officina.R

// Exibido no painel de detalhe quando nenhum projeto está selecionado (tablet).
// No phone este composable nunca é renderizado.
@Composable
fun ProjectDetailPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.placeholder_select_project),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.placeholder_detail_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

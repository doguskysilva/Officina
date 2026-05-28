package com.doguskytech.officina.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.doguskytech.officina.R

@Composable
fun SortProjectsSheet() {
    val sortOptions = listOf(
        stringResource(R.string.sort_az),
        stringResource(R.string.sort_newest),
        stringResource(R.string.sort_oldest),
    )
    Column {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        HorizontalDivider()
        sortOptions.forEach { option ->
            ListItem(
                headlineContent = { Text(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 4.dp),
            )
        }
    }
}

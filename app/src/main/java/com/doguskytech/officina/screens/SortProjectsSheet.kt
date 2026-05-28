package com.doguskytech.officina.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.doguskytech.officina.R
import com.doguskytech.officina.domain.model.SortOrder

@Composable
fun SortProjectsSheet(
    currentSort: SortOrder,
    onSortChange: (SortOrder) -> Unit,
) {
    val options = listOf(
        SortOrder.NAME_ASC to stringResource(R.string.sort_az),
        SortOrder.NEWEST   to stringResource(R.string.sort_newest),
        SortOrder.OLDEST   to stringResource(R.string.sort_oldest),
    )
    Column {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        HorizontalDivider()
        options.forEach { (order, label) ->
            ListItem(
                headlineContent = { Text(label) },
                trailingContent = {
                    if (currentSort == order) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSortChange(order) }
                    .padding(horizontal = 4.dp),
            )
        }
    }
}

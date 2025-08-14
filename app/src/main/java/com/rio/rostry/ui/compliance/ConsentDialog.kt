package com.rio.rostry.ui.compliance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rio.rostry.R

@Composable
fun ConsentDialog(
    onAccept: (dataRetentionAccepted: Boolean) -> Unit,
    onDecline: () -> Unit
) {
    var acceptRetention by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(id = R.string.consent_title)) },
        text = {
            Column {
                Text(stringResource(id = R.string.consent_message))
                Spacer(modifier = Modifier.height(12.dp))
                RowWithCheckbox(
                    checked = acceptRetention,
                    onCheckedChange = { acceptRetention = it },
                    text = stringResource(id = R.string.data_retention_agree)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAccept(acceptRetention) }) {
                Text(stringResource(id = R.string.action_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(id = R.string.action_decline))
            }
        }
    )
}

@Composable
private fun RowWithCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    androidx.compose.foundation.layout.Row {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text)
    }
}

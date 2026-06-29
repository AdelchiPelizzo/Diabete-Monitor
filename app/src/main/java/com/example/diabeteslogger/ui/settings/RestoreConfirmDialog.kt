package com.example.diabeteslogger.ui.settings

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.diabeteslogger.R

@Composable
fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(stringResource(R.string.restore_json_backup))
        },
        text = {
            Text(stringResource(R.string.restore_data_warning))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
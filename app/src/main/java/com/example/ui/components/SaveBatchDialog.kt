package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun SaveBatchDialog(
  defaultTitle: String,
  onDismiss: () -> Unit,
  onConfirm: (title: String, notes: String) -> Unit
) {
  var title by remember { mutableStateOf(defaultTitle) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Save to Batch Log") },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Batch / Run Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_batch_name_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Distiller Notes (Cask #, Grain Bill, Water type)") },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_batch_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(title.ifBlank { defaultTitle }, notes) },
        modifier = Modifier.testTag("dialog_save_confirm_button")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dialog_save_cancel_button")
      ) {
        Text("Cancel")
      }
    }
  )
}

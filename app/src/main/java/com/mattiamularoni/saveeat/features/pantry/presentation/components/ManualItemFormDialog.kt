package com.mattiamularoni.saveeat.features.pantry.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ManualItemFormState(
    val itemName: String = "",
    val category: PantryCategory = PantryCategory.PANTRY,
    val quantity: String = "",
    val unit: String = "",
    val expirationDate: LocalDate? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualItemFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (ManualItemFormState) -> Unit,
    modifier: Modifier = Modifier,
) {
    var formState by remember { mutableStateOf(ManualItemFormState()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Gestione dello stato del DatePicker nativo
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                formState.expirationDate
                    ?.atStartOfDay(ZoneId.systemDefault())
                    ?.toInstant()
                    ?.toEpochMilli(),
        )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        formState = formState.copy(expirationDate = selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Aggiungi Elemento",
                    style = MaterialTheme.typography.headlineSmall,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Chiudi")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // 1. Nome Elemento
                OutlinedTextField(
                    value = formState.itemName,
                    onValueChange = {
                        formState = formState.copy(itemName = it)
                        errorMessage = null
                    },
                    label = { Text("Nome prodotto *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = formState.itemName.isBlank() && errorMessage != null,
                )

                // 2. Categoria (Chips veloci invece del Dropdown)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Categoria", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf(PantryCategory.FRIDGE, PantryCategory.PANTRY, PantryCategory.FREEZER)
                            .forEach { category ->
                                FilterChip(
                                    selected = formState.category == category,
                                    onClick = { formState = formState.copy(category = category) },
                                    label = { Text(formatCategory(category)) },
                                )
                            }
                    }
                }

                // 3. Quantità e Unità affiancate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = formState.quantity,
                        onValueChange = { formState = formState.copy(quantity = it) },
                        label = { Text("Quantità") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = formState.unit,
                        onValueChange = { formState = formState.copy(unit = it) },
                        label = { Text("Unità (es. g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }

                // 4. Data di Scadenza (Finto TextField cliccabile)
                OutlinedTextField(
                    value = formState.expirationDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false, // Lo rende non digitabile ma gestiamo il click sul Modifier
                    label = { Text("Data scadenza (Opzionale)") },
                    trailingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = "Calendario") },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (formState.itemName.isBlank()) {
                        errorMessage = "Inserisci il nome del prodotto"
                    } else {
                        onSubmit(formState)
                    }
                },
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
    )
}

private fun formatCategory(category: PantryCategory): String =
    when (category) {
        PantryCategory.FRIDGE -> "Frigo"
        PantryCategory.PANTRY -> "Dispensa"
        PantryCategory.FREEZER -> "Freezer"
        PantryCategory.ALL -> "Tutti"
    }

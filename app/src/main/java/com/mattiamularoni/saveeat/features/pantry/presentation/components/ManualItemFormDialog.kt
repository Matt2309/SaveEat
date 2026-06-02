package com.mattiamularoni.saveeat.features.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ManualItemFormState(
    val itemName: String = "",
    val category: PantryCategory = PantryCategory.PANTRY,
    val quantity: String = "",
    val unit: String = "",
    val expirationDate: LocalDate? = null,
)

@Composable
fun ManualItemFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (ManualItemFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    var formState by remember { mutableStateOf(ManualItemFormState()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aggiungi Elemento",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error message display
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Item Name Input
                OutlinedTextField(
                    value = formState.itemName,
                    onValueChange = { newValue ->
                        formState = formState.copy(itemName = newValue)
                        errorMessage = null
                    },
                    label = { Text("Nome Elemento *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = formState.itemName.isBlank() && errorMessage != null
                )

                // Category Dropdown
                OutlinedButton(
                    onClick = { showCategoryDropdown = !showCategoryDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Categoria: ${formatCategory(formState.category)}")
                }
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    listOf(PantryCategory.FRIDGE, PantryCategory.PANTRY, PantryCategory.FREEZER)
                        .forEach { category ->
                            DropdownMenuItem(
                                text = { Text(formatCategory(category)) },
                                onClick = {
                                    formState = formState.copy(category = category)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                }

                // Quantity Input
                OutlinedTextField(
                    value = formState.quantity,
                    onValueChange = { newValue ->
                        formState = formState.copy(quantity = newValue)
                    },
                    label = { Text("Quantità (opzionale)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Unit Input
                OutlinedTextField(
                    value = formState.unit,
                    onValueChange = { newValue ->
                        formState = formState.copy(unit = newValue)
                    },
                    label = { Text("Unità - es. g, ml, pezzi (opzionale)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Expiration Date Selector
                OutlinedButton(
                    onClick = { showDatePicker = !showDatePicker },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (formState.expirationDate != null)
                            "Data scadenza: ${formState.expirationDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                        else
                            "Seleziona data scadenza (opzionale)"
                    )
                }

                if (showDatePicker) {
                    DatePickerSection(
                        onDateSelected = { date ->
                            formState = formState.copy(expirationDate = date)
                            showDatePicker = false
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        formState.itemName.isBlank() -> {
                            errorMessage = "Il nome dell'elemento è richiesto"
                        }
                        else -> {
                            onSubmit(formState)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Aggiungi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun DatePickerSection(
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Data scadenza",
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { selectedDate = selectedDate.minusDays(1) },
                modifier = Modifier.weight(0.2f)
            ) {
                Text("-")
            }
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                modifier = Modifier.weight(0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = { selectedDate = selectedDate.plusDays(1) },
                modifier = Modifier.weight(0.2f)
            ) {
                Text("+")
            }
        }
        Button(
            onClick = { onDateSelected(selectedDate) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Conferma data")
        }
    }
}

private fun formatCategory(category: PantryCategory): String {
    return when (category) {
        PantryCategory.FRIDGE -> "Frigo"
        PantryCategory.PANTRY -> "Dispensa"
        PantryCategory.FREEZER -> "Freezer"
        PantryCategory.ALL -> "Tutti"
    }
}

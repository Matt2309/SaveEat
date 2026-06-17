package com.mattiamularoni.saveeat.features.pantry.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableFab(
    onScannerClick: () -> Unit,
    onManualInsertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        // bottom ridotto per abbassare il FAB verso la bottom bar
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(expandFrom = androidx.compose.ui.Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = androidx.compose.ui.Alignment.Bottom) + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        onManualInsertClick()
                        isExpanded = false
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Aggiungi manualmente"
                    )
                }
                FloatingActionButton(
                    onClick = {
                        onScannerClick()
                        isExpanded = false
                    }
                ) {
                    Icon(
                        Icons.Rounded.DocumentScanner,
                        contentDescription = "Scansiona scontrino"
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded }
        ) {
            Icon(
                if (isExpanded) Icons.Rounded.Remove else Icons.Rounded.DocumentScanner,
                contentDescription = if (isExpanded) "Chiudi" else "Aggiungi"
            )
        }
    }
}

package com.mattiamularoni.saveeat.features.scan_receipt.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mattiamularoni.saveeat.ui.theme.SaveEatTheme

/**
 * Stato UI di un singolo ingrediente "fresco" da rivedere.
 * (Modello mock per la @Preview — i dati reali arriveranno dal ViewModel.)
 */
data class ReviewItemUiState(
    val id: String,
    val title: String,
    val imageUrl: String,
    val aiSuggestedDays: Int,
    val currentSelectedDays: Int
)

/**
 * Modale "Smart Expiry Date Review": carosello di ingredienti freschi su cui
 * confermare/aggiustare i giorni di scadenza suggeriti dall'AI.
 *
 * Composable COMPLETAMENTE STATELESS: tutto lo stato è hoisted, la logica di business
 * verrà collegata dal Tech Lead tramite i callback qui sotto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCarouselSheet(
    items: List<ReviewItemUiState>,
    savedLongShelfCount: Int,
    onDecrementDays: (id: String) -> Unit,
    onIncrementDays: (id: String) -> Unit,
    onConfirm: (id: String) -> Unit,
    onSkip: (id: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Tech Lead - fornire/ricordare lo stato dello sheet se serve un controllo esterno.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ReviewCarouselContent(
            items = items,
            savedLongShelfCount = savedLongShelfCount,
            onDecrementDays = onDecrementDays,
            onIncrementDays = onIncrementDays,
            onConfirm = onConfirm,
            onSkip = onSkip
        )
    }
}

/**
 * Contenuto dello sheet, estratto per poterlo mostrare nella @Preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewCarouselContent(
    items: List<ReviewItemUiState>,
    savedLongShelfCount: Int,
    onDecrementDays: (id: String) -> Unit,
    onIncrementDays: (id: String) -> Unit,
    onConfirm: (id: String) -> Unit,
    onSkip: (id: String) -> Unit
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val currentItem = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---- Header ----
        Text(
            text = "Abbiamo salvato $savedLongShelfCount prodotti a lunga scadenza. " +
                "Controlliamo rapidamente questi ${items.size} ingredienti freschi!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // ---- Indicatore "X DI Y" ----
        Text(
            text = "${pagerState.currentPage + 1} DI ${items.size}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // ---- Pager con le card ----
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
        ) { page ->
            ReviewCard(
                item = items[page],
                onDecrement = { onDecrementDays(items[page].id) },
                onIncrement = { onIncrementDays(items[page].id) }
            )
        }

        // ---- Dot indicator ----
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(items.size) { index ->
                val active = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (active) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // ---- Azioni ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { onConfirm(currentItem.id) }, // TODO: Tech Lead - conferma + avanza/chiudi
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Conferma", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(
                onClick = { onSkip(currentItem.id) }, // TODO: Tech Lead - salta (lunga scadenza)
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Salta / Lunga scadenza",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    item: ReviewItemUiState,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ---- Area immagine + badge "Fresco" ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )
                // Badge "Fresco" in alto a sinistra
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Fresco",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- Titolo ----
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // ---- Badge AI ----
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "L'AI suggerisce: ${item.aiSuggestedDays} giorni",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // ---- Stepper giorni ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDecrement,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("-1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.currentSelectedDays.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "GIORNI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = onIncrement,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("+1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Preview
// ----------------------------------------------------------------------------

private val previewItems = listOf(
    ReviewItemUiState("1", "Petto di Pollo Fresco", "", aiSuggestedDays = 3, currentSelectedDays = 3),
    ReviewItemUiState("2", "Insalata Mista", "", aiSuggestedDays = 4, currentSelectedDays = 4),
    ReviewItemUiState("3", "Salmone Fresco", "", aiSuggestedDays = 2, currentSelectedDays = 2),
    ReviewItemUiState("4", "Yogurt Greco", "", aiSuggestedDays = 7, currentSelectedDays = 7)
)

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun ReviewCarouselSheetPreview() {
    SaveEatTheme {
        // In preview mostriamo il contenuto dello sheet (il ModalBottomSheet reale
        // appare come overlay a runtime).
        Surface(color = MaterialTheme.colorScheme.surface) {
            ReviewCarouselContentPreviewWrapper()
        }
    }
}

/** Wrapper di sola preview per richiamare il contenuto privato con dati mock. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewCarouselContentPreviewWrapper() {
    ReviewCarouselContent(
        items = previewItems,
        savedLongShelfCount = 15,
        onDecrementDays = {},
        onIncrementDays = {},
        onConfirm = {},
        onSkip = {}
    )
}

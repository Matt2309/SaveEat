package com.mattiamularoni.saveeat.features.pantry.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset
import com.mattiamularoni.saveeat.features.pantry.presentation.FreshnessLevel
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem

// Colori "freshness" come da mockup
private val FreshnessHigh = Color(0xFF4CAF50)
private val FreshnessMedium = Color(0xFFFFC107)
private val FreshnessCritical = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryTopBar(
    onAvatarClick: () -> Unit = {},
    expiryAlertsEnabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .height(56.dp)
    ) {
        com.mattiamularoni.saveeat.core.ui.UserAvatar(
            size = 32.dp,
            onClick = onAvatarClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
        Text(
            text = "SaveEat",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = { /* TODO: notifiche */ },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            Icon(
                imageVector = if (expiryAlertsEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: PantryCategory,
    onCategorySelected: (PantryCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantryCategory.entries.forEach { category ->
            val selected = category == selectedCategory
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface,
                border = if (selected) null
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.clickable { onCategorySelected(category) }
            ) {
                Text(
                    text = categoryLabel(category),
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PantrySection(
    title: String,
    icon: ImageVector,
    items: List<PantryItem>,
    assets: Map<String, PantryAsset>,
    onAddToShoppingList: (String) -> Unit,
    onDelete: (String) -> Unit,
    onConsume: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header sezione
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = sectionTint(title))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items.forEach { item ->
            key(item.id) {
                if (item.isPlaceholder) {
                    PlaceholderItemCard(item = item, onAddToShoppingList = onAddToShoppingList)
                } else {
                    SwipeablePantryItemCard(
                        item = item,
                        assets = assets,
                        onDelete = onDelete,
                        onConsume = onConsume
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePantryItemCard(
    item: PantryItem,
    assets: Map<String, PantryAsset>,
    onDelete: (String) -> Unit,
    onConsume: (String) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        // Predicate puro: nessun effetto qui, altrimenti Compose può invocarlo più di una
        // volta per uno stesso swipe e l'azione (e il relativo snackbar) scatterebbe due volte.
        confirmValueChange = { it != SwipeToDismissBoxValue.Settled }
    )

    // L'azione vera viene eseguita una sola volta quando currentValue cambia, poi il box
    // viene resettato così lo sfondo colorato collassa invece di restare visibile.
    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> { // destra → elimina
                onDelete(item.id)
                dismissState.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> { // sinistra → consumato
                onConsume(item.id)
                dismissState.reset()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    // +5 punti se il prodotto è in scadenza (CRITICAL/MEDIUM), 0 se a lunga conservazione
    val consumePoints = if (
        item.freshnessLevel == FreshnessLevel.CRITICAL ||
        item.freshnessLevel == FreshnessLevel.MEDIUM
    ) 5 else 0

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground(dismissState.dismissDirection, consumePoints) },
        content = { PantryItemCard(item = item, assets = assets) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue, consumePoints: Int) {
    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error // elimina (rosso)
        SwipeToDismissBoxValue.EndToStart -> FreshnessMedium                 // consumato (giallo)
        else -> Color.Transparent
    }
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd ->
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Elimina",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            SwipeToDismissBoxValue.EndToStart ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (consumePoints > 0) {
                        Text(
                            text = "+$consumePoints punti",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Filled.SoupKitchen,
                        contentDescription = "Consuma",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            else -> {}
        }
    }
}

@Composable
private fun PantryItemCard(item: PantryItem, assets: Map<String, PantryAsset>) {
    val locale = LocalConfiguration.current.locales[0]
    val asset = assets[item.categoryKey]
    val displayName = asset?.names?.get(locale.language)
        ?: asset?.names?.get("en")
        ?: item.name.ifBlank { item.categoryKey }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra verticale freshness
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(freshnessColor(item.freshnessLevel))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetThumbnail(asset = asset)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.expirationLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.freshnessLevel == FreshnessLevel.CRITICAL)
                            FreshnessCritical else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (item.freshnessLevel == FreshnessLevel.CRITICAL)
                            FontWeight.Medium else FontWeight.Normal
                    )
                }
                QuantityPill(item.quantity)
            }
        }
    }
}

@Composable
private fun PlaceholderItemCard(
    item: PantryItem,
    onAddToShoppingList: (String) -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                    )
                )
            }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniatura grigia con icona generica
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${item.name} (Pianificato)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.expirationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            // Pulsante "aggiungi alla lista della spesa"
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = { onAddToShoppingList(item.id) }) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = "Aggiungi alla lista della spesa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetThumbnail(asset: PantryAsset?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (!asset?.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = asset!!.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QuantityPill(quantity: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = quantity,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun freshnessColor(level: FreshnessLevel): Color = when (level) {
    FreshnessLevel.HIGH -> FreshnessHigh
    FreshnessLevel.MEDIUM -> FreshnessMedium
    FreshnessLevel.CRITICAL -> FreshnessCritical
}

@Composable
private fun sectionTint(title: String): Color = when (title) {
    "Dispensa" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private fun categoryLabel(category: PantryCategory): String = when (category) {
    PantryCategory.ALL -> "Tutti"
    PantryCategory.FRIDGE -> "Frigo"
    PantryCategory.PANTRY -> "Dispensa"
    PantryCategory.FREEZER -> "Freezer"
}

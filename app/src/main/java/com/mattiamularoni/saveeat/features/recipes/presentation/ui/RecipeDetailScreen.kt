package com.mattiamularoni.saveeat.features.recipes.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mattiamularoni.saveeat.features.pantry.presentation.state.PantryUiState
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.recipes.presentation.state.RecipeUiState
import com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    id: String,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    recipeViewModel: RecipeViewModel = koinViewModel(),
    pantryViewModel: PantryViewModel = koinViewModel()
) {
    val recipesState by recipeViewModel.recipesUiState.collectAsState()
    val pantryState by pantryViewModel.uiState.collectAsState()

    val recipe = (recipesState as? RecipeUiState.Success)?.recipes?.firstOrNull { it.id == id }

    val pantryNames = remember(pantryState) {
        (pantryState as? PantryUiState.Success)?.items
            ?.filter { !it.isPlaceholder }
            ?.map { it.name.lowercase().trim() }
            ?: emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            if (recipe != null) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Button(
                        onClick = { /* TODO: segna come cucinata (azione backend non disponibile) */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Segna come cucinata", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        if (recipe == null) {
            if (recipesState is RecipeUiState.Loading) {
                com.mattiamularoni.saveeat.core.ui.SaveEatLoadingSkeleton(
                    modifier = Modifier.padding(padding)
                )
            } else {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Ricetta non trovata", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            DetailContent(
                recipe = recipe,
                pantryNames = pantryNames,
                onNavigateBack = onNavigateBack,
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun DetailContent(
    recipe: Recipe,
    pantryNames: List<String>,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues
) {
    var isFavorite by remember { mutableStateOf(false) }
    val steps = remember(recipe.instructions) { splitSteps(recipe.instructions) }
    val description = remember(recipe.instructions) { firstSentence(recipe.instructions) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        // ---- Header immagine con back + cuore ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            if (!recipe.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") { onNavigateBack() }
                CircleIconButton(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    "Preferiti",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ) { isFavorite = !isFavorite }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titolo + descrizione
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chip meta
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${recipe.prepTimeMinutes} min",
                    container = MaterialTheme.colorScheme.surfaceContainerLow,
                    content = MaterialTheme.colorScheme.onSurface
                )
                // Placeholder: difficoltà derivata dal tempo
                MetaChip(
                    icon = Icons.Filled.Restaurant,
                    text = difficultyLabel(recipe.prepTimeMinutes),
                    container = MaterialTheme.colorScheme.surfaceContainerLow,
                    content = MaterialTheme.colorScheme.onSurface
                )
                // Placeholder: punti
                MetaChip(
                    icon = Icons.Filled.Eco,
                    text = "+50 pt",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Ingredienti
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingredienti",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    recipe.ingredients.forEach { ing ->
                        IngredientRow(
                            label = ingredientLabel(ing),
                            inPantry = ingredientInPantry(ing.name, pantryNames)
                        )
                    }
                    if (recipe.ingredients.isEmpty()) {
                        Text(
                            "Nessun ingrediente indicato.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Preparazione
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Preparazione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                steps.forEachIndexed { index, step ->
                    StepRow(number = index + 1, text = step, isFirst = index == 0, isLast = index == steps.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    container: Color,
    content: Color
) {
    Surface(shape = RoundedCornerShape(8.dp), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = content)
        }
    }
}

@Composable
private fun IngredientRow(label: String, inPantry: Boolean) {
    if (inPantry) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    "In Dispensa",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(32.dp)
            ) {
                IconButton(onClick = { /* TODO: aggiungi alla lista della spesa (placeholder) */ }) {
                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String, isFirst: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isFirst) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

// ---- Helpers ----

private fun firstSentence(instructions: String): String {
    val clean = instructions.trim()
    if (clean.isEmpty()) return ""
    val end = clean.indexOf(". ")
    return if (end > 0) clean.substring(0, end + 1) else clean.lineSequence().firstOrNull()?.trim().orEmpty()
}

private fun splitSteps(instructions: String): List<String> {
    val byLine = instructions.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    if (byLine.size > 1) return byLine.map { it.trimStart('0','1','2','3','4','5','6','7','8','9','.',')',' ','-') }
    // singola riga: spezzo per frasi
    return instructions.split(Regex("(?<=\\.)\\s+")).map { it.trim() }.filter { it.isNotBlank() }
}

private fun difficultyLabel(prepTimeMinutes: Int): String = when {
    prepTimeMinutes <= 20 -> "Facile"
    prepTimeMinutes <= 40 -> "Media"
    else -> "Impegnativa"
}

private fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()

private fun ingredientLabel(ing: Recipe.Ingredient): String {
    val amt = formatAmount(ing.amount)
    val unit = ing.unit.trim()
    val inside = when {
        unit.isEmpty() -> amt
        unit.length <= 2 -> "$amt$unit"
        else -> "$amt $unit"
    }
    return "${ing.name} ($inside)"
}

private fun ingredientInPantry(name: String, pantry: List<String>): Boolean {
    val key = name.lowercase().substringBefore("(").trim()
    if (key.isBlank()) return false
    val first = key.split(" ").firstOrNull().orEmpty()
    return pantry.any { p ->
        p.isNotBlank() && (p.contains(key) || key.contains(p) || (first.length >= 3 && p.contains(first)))
    }
}

package com.mattiamularoni.saveeat.features.recipes.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mattiamularoni.saveeat.features.recipes.domain.model.RecipeFilters
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.recipes.presentation.state.GenerateRecipeUiState
import com.mattiamularoni.saveeat.features.recipes.presentation.state.RecipeUiState
import com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val Filters = listOf("Suggeriti", "Veloci (< 30 min)", "Vegetariani")
private val CuisineStyles = listOf("Italiana", "Asiatica", "Messicana")
private val TimingOptions = listOf("Veloce (15 min)" to "veloce", "Medio (30 min)" to "medio")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenRecipe: (String) -> Unit = {},
    viewModel: RecipeViewModel = koinViewModel()
) {
    val uiState by viewModel.recipesUiState.collectAsState()
    val generateState by viewModel.generateRecipeUiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(Filters.first()) }
    var showModal by remember { mutableStateOf(false) }

    val notificationPreferencesController: com.mattiamularoni.saveeat.ui.settings.NotificationPreferencesController =
        koinInject()
    val expiryAlertsEnabled by notificationPreferencesController.expiryAlertsEnabled.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = { RecipeTopBar(onAvatarClick = onNavigateToProfile, expiryAlertsEnabled = expiryAlertsEnabled) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intestazione
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Usa i tuoi ingredienti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Suggerimenti smart basati sulla tua dispensa per evitare sprechi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bottone Inventa Ricetta
            Button(
                onClick = { showModal = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Inventa Ricetta",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Chip filtro (solo visivi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Filters.forEach { filter ->
                    FilterPill(
                        label = filter,
                        selected = filter == selectedFilter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            // Contenuto
            when (val state = uiState) {
                is RecipeUiState.Loading -> {
                    com.mattiamularoni.saveeat.core.ui.SaveEatLoadingSkeleton(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is RecipeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                is RecipeUiState.Empty -> {
                    Text(
                        text = "Nessuna ricetta disponibile al momento.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                is RecipeUiState.Success -> {
                    val recipes = state.recipes
                    // Prima ricetta in evidenza
                    recipes.firstOrNull()?.let { recipe ->
                        FeaturedRecipeCard(recipe, onClick = { onOpenRecipe(recipe.id) })
                    }
                    // Le altre, a coppie
                    recipes.drop(1).chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { recipe ->
                                SmallRecipeCard(
                                    recipe,
                                    Modifier.weight(1f),
                                    onClick = { onOpenRecipe(recipe.id) }
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Modal bottom sheet per generazione ricette
    if (showModal) {
        GenerateRecipeModal(
            generateState = generateState,
            onDismiss = {
                showModal = false
                viewModel.resetGenerateState()
            },
            onGenerate = { filters ->
                viewModel.generateFromPantry(filters)
            },
            onSuccess = {
                showModal = false
                viewModel.resetGenerateState()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenerateRecipeModal(
    generateState: GenerateRecipeUiState,
    onDismiss: () -> Unit,
    onGenerate: (RecipeFilters) -> Unit,
    onSuccess: () -> Unit
) {
    var selectedCuisine by remember { mutableStateOf<String?>(null) }
    var selectedTiming by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(generateState) {
        if (generateState is GenerateRecipeUiState.Success) {
            onSuccess()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Inventa una Ricetta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Basata sugli ingredienti in scadenza nella tua dispensa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stile culinario
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Stile culinario (opzionale)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CuisineStyles.forEach { cuisine ->
                        val isSelected = cuisine.lowercase() == selectedCuisine
                        FilterPill(
                            label = cuisine,
                            selected = isSelected,
                            onClick = {
                                selectedCuisine = if (isSelected) null else cuisine.lowercase()
                            }
                        )
                    }
                }
            }

            // Tempo di preparazione
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tempo di preparazione (opzionale)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimingOptions.forEach { (label, value) ->
                        val isSelected = value == selectedTiming
                        FilterPill(
                            label = label,
                            selected = isSelected,
                            onClick = {
                                selectedTiming = if (isSelected) null else value
                            }
                        )
                    }
                }
            }

            // Errore
            if (generateState is GenerateRecipeUiState.Error) {
                Text(
                    text = generateState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Bottone genera
            Button(
                onClick = {
                    onGenerate(RecipeFilters(cuisineStyle = selectedCuisine, timingPreference = selectedTiming))
                },
                enabled = generateState !is GenerateRecipeUiState.Generating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (generateState is GenerateRecipeUiState.Generating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Generazione in corso...")
                } else {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Genera Ricette",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeTopBar(onAvatarClick: () -> Unit = {}, expiryAlertsEnabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.mattiamularoni.saveeat.core.ui.UserAvatar(size = 32.dp, onClick = onAvatarClick)
        Text(
            text = "SaveEat",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = { /* TODO: notifiche */ }) {
            Icon(
                imageVector = if (expiryAlertsEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = CircleShape,
        color = bg,
        border = if (selected) null
        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FeaturedRecipeCard(recipe: Recipe, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
    ) {
        // Box grigio al posto della foto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = "Salva",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TimeRow(recipe.prepTimeMinutes)
            TagsRow(recipe.tags, max = 3)
        }
    }
}

@Composable
private fun SmallRecipeCard(recipe: Recipe, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TimeRow(recipe.prepTimeMinutes)
            TagsRow(recipe.tags, max = 1)
        }
    }
}

@Composable
private fun TimeRow(prepTimeMinutes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$prepTimeMinutes min",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TagsRow(tags: List<String>, max: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.take(max).forEach { tag ->
            val isVeg = tag.contains("veget", ignoreCase = true)
            val bg = if (isVeg) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
            val fg = if (isVeg) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
            Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

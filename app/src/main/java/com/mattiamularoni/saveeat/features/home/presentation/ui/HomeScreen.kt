package com.mattiamularoni.saveeat.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.mattiamularoni.saveeat.BuildConfig
import com.mattiamularoni.saveeat.features.notifications.data.worker.NotificationWorker
import com.mattiamularoni.saveeat.features.home.domain.repository.ExpiringItem
import com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard
import com.mattiamularoni.saveeat.features.home.presentation.state.HomeUiState
import com.mattiamularoni.saveeat.features.home.presentation.viewmodel.HomeViewModel
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ExpandableFab
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ManualItemFormDialog
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.ceil

// Colori "freshness" come da mockup
private val FreshnessCritical = Color(0xFFF44336)
private val FreshnessMedium = Color(0xFFFFC107)
private val FreshnessHigh = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToScan: () -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    pantryViewModel: PantryViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var showManualForm by remember { mutableStateOf(false) }
    val uiState by homeViewModel.uiState.collectAsState()

    if (showManualForm) {
        ManualItemFormDialog(
            onDismiss = { showManualForm = false },
            onSubmit = { formState ->
                pantryViewModel.onManualItemInsert(formState)
                showManualForm = false
            }
        )
    }

    val firstName = homeViewModel.currentUserName
        .trim()
        .substringBefore(' ')
        .ifBlank { "Utente" }
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    val notificationPreferencesController: com.mattiamularoni.saveeat.ui.settings.NotificationPreferencesController =
        org.koin.compose.koinInject()
    val expiryAlertsEnabled by notificationPreferencesController.expiryAlertsEnabled.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        // Gli insets sono già gestiti da MainScaffold: evitiamo il doppio inset
        // (striscia bianca in basso + FAB troppo in alto).
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            HomeTopBar(
                avatarUrl = (uiState as? HomeUiState.Success)?.dashboard?.userProfile?.avatarUrl,
                expiryAlertsEnabled = expiryAlertsEnabled,
                onAvatarClick = onNavigateToProfile,
                onNotificationsClick = {
                    if (BuildConfig.DEBUG) {
                        WorkManager.getInstance(context)
                            .enqueue(OneTimeWorkRequestBuilder<NotificationWorker>().build())
                    }
                }
            )
        },
        floatingActionButton = {
            ExpandableFab(
                onScannerClick = onNavigateToScan,
                onManualInsertClick = { showManualForm = true }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { homeViewModel.refreshDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    com.mattiamularoni.saveeat.core.ui.SaveEatLoadingSkeleton(
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
                is HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                is HomeUiState.Empty -> {
                    EmptyState(
                        firstName = firstName,
                        onScan = onNavigateToScan,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    val userStats by homeViewModel.userStats.collectAsState()
                    DashboardContent(
                        firstName = firstName,
                        dashboard = state.dashboard,
                        kgSaved = userStats.totalKgSaved,
                        onSeeAllExpiring = onNavigateToPantry,
                        onOpenRecipe = onNavigateToRecipes
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    avatarUrl: String?,
    expiryAlertsEnabled: Boolean = true,
    onAvatarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar condiviso (foto locale > foto Google > icona)
        com.mattiamularoni.saveeat.core.ui.UserAvatar(size = 32.dp, onClick = onAvatarClick)

        Text(
            text = "SaveEat",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IconButton(onClick = onNotificationsClick) {
            Icon(
                imageVector = if (expiryAlertsEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DashboardContent(
    firstName: String,
    dashboard: HomeDashboard,
    kgSaved: Double,
    onSeeAllExpiring: () -> Unit,
    onOpenRecipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ---- Saluto + Eco-punti ----
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Bentornato, $firstName!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EcoPointsCard(ecoPoints = dashboard.userStats.ecoPoints)
        }

        // ---- In scadenza ----
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "In scadenza",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vedi tutto",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSeeAllExpiring() }
                )
            }

            if (dashboard.expiringItems.isEmpty()) {
                Text(
                    text = "Nessun alimento in scadenza. Ottimo lavoro!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(dashboard.expiringItems, key = { it.id }) { item ->
                        ExpiringItemCard(item)
                    }
                }
            }
        }

        // ---- Bento: stat + ricetta ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SavedFoodCard(kgSaved = kgSaved, modifier = Modifier.weight(1f))
            RecipeCard(
                title = dashboard.suggestedRecipes.firstOrNull()?.title ?: "Nessun suggerimento",
                onClick = onOpenRecipe,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EcoPointsCard(ecoPoints: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Eco-punti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = formatThousands(ecoPoints),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 48.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpiringItemCard(item: ExpiringItem) {
    val days = daysUntil(item.expirationDate)
    val barColor = when {
        days <= 1 -> FreshnessCritical
        days <= 4 -> FreshnessMedium
        else -> FreshnessHigh
    }
    val (label, labelColor) = when {
        days < 0 -> "Scaduto" to FreshnessCritical
        days == 0L -> "Scade oggi" to FreshnessCritical
        days == 1L -> "Domani" to FreshnessCritical
        days <= 4 -> "Tra $days giorni" to Color(0xFFB26A00)
        else -> "Tra $days giorni" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Barra verticale freshness
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(barColor)
                .align(Alignment.CenterStart)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Box grigio neutro al posto della foto + badge categoria
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
private fun SavedFoodCard(kgSaved: Double, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Icon(
            Icons.Outlined.DeleteOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingBasket,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(kgSaved),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "Cibo salvato cucinando ricette",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Surface(
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "RICETTA PER TE",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyState(
    firstName: String,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Bentornato, $firstName!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "La tua dispensa è vuota. Scansiona uno scontrino per iniziare.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onScan) { Text("Scansiona scontrino") }
    }
}

// ---- Helpers ----

private fun daysUntil(expirationMs: Long): Long {
    val diff = expirationMs - System.currentTimeMillis()
    return ceil(diff.toDouble() / (1000L * 60 * 60 * 24)).toLong()
}

private fun formatThousands(value: Int): String {
    // 1250 -> "1,250"
    return "%,d".format(value)
}

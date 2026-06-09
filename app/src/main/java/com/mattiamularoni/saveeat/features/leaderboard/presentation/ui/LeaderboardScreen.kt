package com.mattiamularoni.saveeat.features.leaderboard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.leaderboard.presentation.LeaderboardUserUi
import com.mattiamularoni.saveeat.features.leaderboard.presentation.state.LeaderboardUiState
import com.mattiamularoni.saveeat.features.leaderboard.presentation.viewmodel.LeaderboardViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val Gold = Color(0xFFFFC107)
private val Silver = Color(0xFFBDBDBD)
private val Bronze = Color(0xFFE18500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: LeaderboardViewModel = koinViewModel(),
    sessionProvider: SessionProvider = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = remember { sessionProvider.getCurrentUserId() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { LeaderboardTopBar(onAvatarClick = onNavigateToProfile) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is LeaderboardUiState.Loading ->
                    com.mattiamularoni.saveeat.core.ui.SaveEatLoadingSkeleton(
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                is LeaderboardUiState.Error ->
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )

                is LeaderboardUiState.Success ->
                    LeaderboardContent(users = state.users, currentUserId = currentUserId)
            }
        }
    }
}

@Composable
private fun LeaderboardTopBar(onAvatarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profilo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
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
                Icons.Outlined.Notifications,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LeaderboardContent(users: List<LeaderboardUserUi>, currentUserId: String) {
    val currentUser = users.firstOrNull { it.id == currentUserId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Intestazione
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Classifica Globale",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Confrontati con la community e scala la vetta dell'ecosostenibilità.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // La tua posizione (solo se presente nella lista)
        if (currentUser != null) {
            YourPositionCard(currentUser)
        }

        // Podio + lista
        if (users.isEmpty()) {
            Text(
                text = "Nessun utente nella classifica.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LeaderboardBoard(users = users, currentUser = currentUser)
        }
    }
}

@Composable
private fun YourPositionCard(user: LeaderboardUserUi) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    LbAvatar(user.avatarUrl, user.displayName, 64.dp, ringColor = MaterialTheme.colorScheme.primary)
                    Surface(shape = CircleShape, color = Gold) {
                        Text(
                            text = "#${user.rank}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "La tua posizione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "%,d Eco-punti".format(user.ecoPoints),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            // Badge livello (placeholder visivi)
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LevelBadge(Icons.Filled.Autorenew, "Lvl 3")
                    LevelBadge(Icons.Rounded.Kitchen, "Lvl 2")
                    LevelBadge(Icons.Filled.Restaurant, "Lvl 1")
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun LeaderboardBoard(users: List<LeaderboardUserUi>, currentUser: LeaderboardUserUi?) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            // Podio top 3
            Podium(users.take(3))

            val rest = users.drop(3).filter { it.id != currentUser?.id }
            if (rest.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                rest.forEach { user ->
                    LeaderboardRow(user, highlighted = false)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                }
            }

            // Riga "Tu" (solo se fuori dal podio)
            if (currentUser != null && currentUser.rank > 3) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text("•••", color = MaterialTheme.colorScheme.outline)
                }
                LeaderboardRow(currentUser, highlighted = true, nameOverride = "Tu")
            }
        }
    }
}

@Composable
private fun Podium(top: List<LeaderboardUserUi>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        top.getOrNull(1)?.let { PodiumItem(it, Silver, big = false, modifier = Modifier.weight(1f)) }
            ?: Spacer(Modifier.weight(1f))
        top.getOrNull(0)?.let { PodiumItem(it, Gold, big = true, modifier = Modifier.weight(1f)) }
            ?: Spacer(Modifier.weight(1f))
        top.getOrNull(2)?.let { PodiumItem(it, Bronze, big = false, modifier = Modifier.weight(1f)) }
            ?: Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun PodiumItem(user: LeaderboardUserUi, accent: Color, big: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            if (big) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp).align(Alignment.TopCenter).offset(y = (-16).dp)
                )
            }
            LbAvatar(user.avatarUrl, user.displayName, if (big) 76.dp else 60.dp, ringColor = accent)
            Surface(shape = CircleShape, color = accent, modifier = Modifier.offset(y = 10.dp)) {
                Text(
                    text = user.rank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "%,d pt".format(user.ecoPoints),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (big) FontWeight.Bold else FontWeight.Normal,
            color = if (big) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LeaderboardRow(
    user: LeaderboardUserUi,
    highlighted: Boolean,
    nameOverride: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = user.rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(min = 24.dp)
        )
        LbAvatar(user.avatarUrl, user.displayName, 36.dp)
        Text(
            text = nameOverride ?: user.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "%,d pt".format(user.ecoPoints),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LbAvatar(url: String?, name: String, size: Dp, ringColor: Color? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (ringColor != null) Modifier.border(3.dp, ringColor, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(model = url, contentDescription = name, modifier = Modifier.fillMaxSize().clip(CircleShape))
        } else {
            Text(
                text = name.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

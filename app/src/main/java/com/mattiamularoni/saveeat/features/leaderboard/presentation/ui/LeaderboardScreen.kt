package com.mattiamularoni.saveeat.features.leaderboard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mattiamularoni.saveeat.features.leaderboard.presentation.LeaderboardUserUi
import com.mattiamularoni.saveeat.features.leaderboard.presentation.RankingBadge
import com.mattiamularoni.saveeat.features.leaderboard.presentation.state.LeaderboardUiState
import com.mattiamularoni.saveeat.features.leaderboard.presentation.viewmodel.LeaderboardViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LeaderboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard Globale") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is LeaderboardUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }

            is LeaderboardUiState.Success -> {
                val state = uiState as LeaderboardUiState.Success
                SuccessStateContent(
                    users = state.users,
                    onRefresh = { viewModel.onRefresh() },
                    modifier = Modifier.padding(padding)
                )
            }

            is LeaderboardUiState.Error -> {
                val state = uiState as LeaderboardUiState.Error
                ErrorStateContent(
                    message = state.message,
                    onRetry = { viewModel.onRefresh() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessStateContent(
    users: List<LeaderboardUserUi>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nessun utente nella leaderboard")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(users) { index, user ->
                    LeaderboardRow(user = user)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aggiorna")
            }
        }
    }
}

@Composable
private fun ErrorStateContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Errore nel caricamento",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onRetry) {
            Text("Riprova")
        }
    }
}

@Composable
private fun LeaderboardRow(user: LeaderboardUserUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = getRankBadgeColor(user.badge),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (user.badge) {
                RankingBadge.GOLD, RankingBadge.SILVER, RankingBadge.BRONZE -> Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = user.badge.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                RankingBadge.NONE -> Text(
                    text = user.rank.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // User info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Points
        Text(
            text = user.formattedPoints,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun getRankBadgeColor(badge: RankingBadge): Color {
    return when (badge) {
        RankingBadge.GOLD -> Color(0xFFFFD700)
        RankingBadge.SILVER -> Color(0xFFC0C0C0)
        RankingBadge.BRONZE -> Color(0xFFCD7F32)
        RankingBadge.NONE -> MaterialTheme.colorScheme.primary
    }
}

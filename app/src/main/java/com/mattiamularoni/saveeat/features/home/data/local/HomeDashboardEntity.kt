package com.mattiamularoni.saveeat.features.home.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room per il caching della dashboard Home.
 *
 * Rappresenta i dati aggregati della dashboard, aggiornati da fetch remoti.
 * Utilizzata per supportare offline access e minimizzare chiamate API ripetute.
 *
 * Struttura:
 * - userId: identifier utente (primary key)
 * - expiringItemsJson: lista JSON degli item in scadenza (serializzato)
 * - topLeaderboardJson: lista JSON top 3 utenti leaderboard (serializzato)
 * - suggestedRecipesJson: lista JSON ricette suggerite (serializzato)
 * - userStatsJson: statistiche aggregati (eco_points, pantry count, placeholders count) (serializzato)
 * - userProfileJson: dati profilo utente (name, avatar, rank) (serializzato)
 * - lastSyncedAt: timestamp ultimo refresh (ms from epoch)
 * - createdAt: timestamp creazione record
 */
@Entity(tableName = "home_dashboard")
data class HomeDashboardEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "expiring_items_json")
    val expiringItemsJson: String,
    @ColumnInfo(name = "top_leaderboard_json")
    val topLeaderboardJson: String,
    @ColumnInfo(name = "suggested_recipes_json")
    val suggestedRecipesJson: String,
    @ColumnInfo(name = "user_stats_json")
    val userStatsJson: String,
    @ColumnInfo(name = "user_profile_json")
    val userProfileJson: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

package com.mattiamularoni.saveeat.features.home.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface per il modulo Home.
 *
 * Responsabilità:
 * - Orchestrare remote datasource + local cache
 * - Aggregazione dati dashboard (pantry, leaderboard, recipes)
 * - Gestione sincronizzazione e refresh
 * - Operare su Dispatchers.IO (async, no Main Thread blocking)
 */
interface HomeRepository {
    /**
     * Osserva i dati completi della dashboard Home con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Cache in Room (upsert)
     * - Flow primario da Room (aggiornamenti quando cambiano)
     * - Emette HomeDashboard quando dati cambiano
     *
     * @return Flow della dashboard aggiornato
     */
    fun observeHomeDashboard(): Flow<HomeDashboard?>

    /**
     * Sincronizza la dashboard con Supabase.
     *
     * Logica:
     * - Fetch aggregati dati dashboard
     * - Cache in Room (upsert con REPLACE strategy)
     * - Ritorna true se successo, false se errore
     *
     * Operazione: Dispatchers.IO (remote call)
     *
     * @return true se sync riuscito, false altrimenti
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun refreshHomeDashboard(): Boolean

    /**
     * Recupera snapshot singolo della dashboard.
     *
     * Legge dalla cache Room locale (operazione sincrona).
     *
     * @return HomeDashboard se presente in cache, null altrimenti
     */
    suspend fun getHomeDashboard(): HomeDashboard?
}

/**
 * Modello di dominio per la dashboard Home.
 *
 * Rappresenta la vista aggregata completa della home con tutti i dati necessari
 * per il rendering della UI dashboard.
 *
 * Include:
 * - Expiring items dalla pantry
 * - Top leaderboard users
 * - Suggested recipes matched con expiring ingredients
 * - User statistics (totals, eco points)
 * - User profile (name, avatar, rank)
 */
data class HomeDashboard(
    val userId: String,
    val expiringItems: List<ExpiringItem>,
    val topLeaderboardUsers: List<LeaderboardUser>,
    val suggestedRecipes: List<SuggestedRecipe>,
    val userStats: UserStats,
    val userProfile: UserProfile,
    val lastSyncedAt: Long
)

/**
 * Modello di dominio per un item della pantry in scadenza.
 *
 * Include solo i campi rilevanti per la visualizzazione nella dashboard home.
 */
data class ExpiringItem(
    val id: String,
    val name: String,
    val category: String,
    val categoryKey: String? = null,
    val expirationDate: Long,
    val quantity: Double? = null,
    val unit: String? = null
)

/**
 * Modello di dominio per un utente nel leaderboard.
 *
 * Include dati di ranking e profilo per la snippet leaderboard sulla home.
 */
data class LeaderboardUser(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val ecoPoints: Int = 0
)

/**
 * Modello di dominio per una ricetta suggerita.
 *
 * Include metadati essenziali per la card ricetta sulla dashboard home.
 * matchingIngredients conta quanti ingredienti matchano con expiring items.
 */
data class SuggestedRecipe(
    val id: String,
    val title: String,
    val tags: List<String> = emptyList(),
    val prepTimeMinutes: Int? = null,
    val matchingIngredients: Int = 0,
    val imageUrl: String? = null
)

/**
 * Modello di dominio per le statistiche aggregate della pantry.
 *
 * Contiene metriche di sintesi della dispensa dell'utente per la dashboard.
 */
data class UserStats(
    val totalItems: Int = 0,
    val expiringCount: Int = 0,
    val activePlaceholders: Int = 0,
    val ecoPoints: Int = 0
)

/**
 * Modello di dominio per i dati profilo dell'utente.
 *
 * Include informazioni di identità e ranking per la home dashboard.
 */
data class UserProfile(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val rankPosition: Int = 0
)

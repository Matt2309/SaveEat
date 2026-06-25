package com.mattiamularoni.saveeat.features.home.data.remote

import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryItemDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant

class HomeRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
) : HomeRemoteDataSource {
    override suspend fun getHomeDashboard(userId: String): HomeDashboardDto =
        withContext(Dispatchers.IO) {
            try {
                val expiringItems = getExpiringItems(userId)
                val userStats = getUserStats(userId)
                val topLeaderboard = getTopLeaderboardUsers(3)
                val userProfile = getUserProfile(userId)
                val rankPosition = getUserRankPosition(userId)
                val recipes = getAllRecipes()
                val suggestedRecipes = matchRecipesToExpiringItems(recipes, expiringItems)

                HomeDashboardDto(
                    userId = userId,
                    expiringItems = expiringItems,
                    topLeaderboard = topLeaderboard,
                    suggestedRecipes = suggestedRecipes,
                    userStats = userStats,
                    userProfile = userProfile.copy(rankPosition = rankPosition),
                    lastSyncedAt = System.currentTimeMillis().toString(),
                )
            } catch (e: Exception) {
                throw Exception("Failed to fetch home dashboard: ${e.message}", e)
            }
        }

    override suspend fun getExpiringItems(userId: String): List<ExpiringItemDto> =
        withContext(Dispatchers.IO) {
            try {
                val sevenDaysFromNow = Instant.now().plus(Duration.ofDays(7)).toString()
                supabaseClient
                    .from("pantry_items")
                    .select {
                        filter {
                            eq("user_id", userId)
                            lte("expiration_date", sevenDaysFromNow)
                        }
                        order("expiration_date", Order.ASCENDING)
                    }.decodeList<ExpiringItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch expiring items: ${e.message}", e)
            }
        }

    override suspend fun getUserStats(userId: String): UserStatsDto =
        withContext(Dispatchers.IO) {
            try {
                val activeItems =
                    supabaseClient
                        .from("pantry_items")
                        .select {
                            filter {
                                eq("user_id", userId)
                                eq("status", "ACTIVE")
                            }
                        }.decodeList<PantryItemDto>()

                val expiryThreshold = Instant.now().plus(Duration.ofDays(7)).toString()
                // eco_points: unica fonte di verità è user_stats.total_eco_points, esposta dalla vista `leaderboard`.
                val ecoPoints =
                    supabaseClient
                        .from("leaderboard")
                        .select { filter { eq("id", userId) } }
                        .decodeList<LeaderboardUserDto>()
                        .firstOrNull()
                        ?.ecoPoints ?: 0

                UserStatsDto(
                    totalItems = activeItems.size,
                    expiringCount =
                        activeItems.count {
                            it.expirationDate != null && it.expirationDate <= expiryThreshold
                        },
                    activePlaceholders = activeItems.count { it.isPlaceholder },
                    ecoPoints = ecoPoints,
                )
            } catch (e: Exception) {
                throw Exception("Failed to fetch user stats: ${e.message}", e)
            }
        }

    override suspend fun getTopLeaderboardUsers(limit: Int): List<LeaderboardUserDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("leaderboard")
                    .select {
                        order("eco_points", Order.DESCENDING)
                        limit(limit.toLong())
                    }.decodeList<LeaderboardUserDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch top leaderboard users: ${e.message}", e)
            }
        }

    override suspend fun getUserRankPosition(userId: String): Int =
        withContext(Dispatchers.IO) {
            try {
                val userEcoPoints =
                    supabaseClient
                        .from("leaderboard")
                        .select { filter { eq("id", userId) } }
                        .decodeList<LeaderboardUserDto>()
                        .firstOrNull()
                        ?.ecoPoints ?: 0

                val usersAbove =
                    supabaseClient
                        .from("leaderboard")
                        .select { filter { gt("eco_points", userEcoPoints) } }
                        .decodeList<LeaderboardUserDto>()

                usersAbove.size + 1
            } catch (e: Exception) {
                throw Exception("Failed to fetch user rank position: ${e.message}", e)
            }
        }

    override suspend fun getAllRecipes(): List<SuggestedRecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("recipes")
                    .select()
                    .decodeList<SuggestedRecipeDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes: ${e.message}", e)
            }
        }

    override suspend fun getUserProfile(userId: String): UserProfileDto =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeList<UserProfileDto>()
                    .firstOrNull() ?: UserProfileDto(id = userId)
            } catch (e: Exception) {
                throw Exception("Failed to fetch user profile: ${e.message}", e)
            }
        }

    private fun matchRecipesToExpiringItems(
        recipes: List<SuggestedRecipeDto>,
        expiringItems: List<ExpiringItemDto>,
    ): List<SuggestedRecipeDto> {
        val expiringNames = expiringItems.map { it.name.lowercase() }

        val scoredRecipes =
            recipes.map { recipe ->
                val tagList: List<String> =
                    try {
                        Json
                            .decodeFromString<List<String>>(recipe.tags ?: "[]")
                            .map { it.lowercase() }
                    } catch (_: Exception) {
                        recipe.title.lowercase().split(" ")
                    }

                val matchCount =
                    tagList.count { tag ->
                        expiringNames.any { name -> name.contains(tag) || tag.contains(name) }
                    }
                recipe.copy(matchingIngredients = matchCount)
            }

        return scoredRecipes
            .filter { it.matchingIngredients > 0 }
            .sortedByDescending { it.matchingIngredients }
            .take(5)
    }
}

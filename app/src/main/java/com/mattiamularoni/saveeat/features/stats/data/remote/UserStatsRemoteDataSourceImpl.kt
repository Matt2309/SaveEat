package com.mattiamularoni.saveeat.features.stats.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementazione di UserStatsRemoteDataSource usando Supabase Postgrest.
 */
class UserStatsRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : UserStatsRemoteDataSource {

    override suspend fun getUserStats(userId: String): UserStatsDto? =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("user_stats")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<UserStatsDto>()
                    .firstOrNull()
            } catch (e: Exception) {
                throw Exception("Failed to fetch user stats: ${e.message}", e)
            }
        }

    override suspend fun upsertUserStats(dto: UserStatsDto): UserStatsDto =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("user_stats")
                    .upsert(dto) {
                        select()
                    }
                    .decodeSingle<UserStatsDto>()
            } catch (e: Exception) {
                throw Exception("Failed to upsert user stats: ${e.message}", e)
            }
        }
}

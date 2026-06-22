package com.mattiamularoni.saveeat.features.stats.data.repository

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.stats.data.local.UserStatsDao
import com.mattiamularoni.saveeat.features.stats.data.local.UserStatsEntity
import com.mattiamularoni.saveeat.features.stats.data.remote.UserStatsDto
import com.mattiamularoni.saveeat.features.stats.data.remote.UserStatsRemoteDataSource
import com.mattiamularoni.saveeat.features.stats.domain.model.UserStats
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementazione di StatsRepository con sincronizzazione Supabase e caching Room.
 *
 * Relazione 1-a-1 con l'utente: una singola riga per user_id sia in Room che su Supabase.
 *
 * NOTE (atomicità): addRecipeCookedStats legge i totali correnti e poi scrive il nuovo totale
 * (read-then-write). Sotto cucinate concorrenti dello stesso utente questo può perdere un
 * incremento. L'alternativa atomica sarebbe una RPC Postgres, es.:
 *   create function increment_user_stats(p_user_id uuid, p_kg numeric, p_euros numeric, p_points int)
 *   returns void as $$
 *     insert into user_stats (user_id, total_kg_saved, total_euros_saved, total_eco_points)
 *     values (p_user_id, p_kg, p_euros, p_points)
 *     on conflict (user_id) do update set
 *       total_kg_saved = user_stats.total_kg_saved + excluded.total_kg_saved,
 *       total_euros_saved = user_stats.total_euros_saved + excluded.total_euros_saved,
 *       total_eco_points = user_stats.total_eco_points + excluded.total_eco_points;
 *   $$ language sql;
 * chiamata via supabaseClient.postgrest.rpc("increment_user_stats", ...).
 * TODO(atomic): migrare ad una RPC quando le cucinate concorrenti diventano un problema reale.
 */
class StatsRepositoryImpl(
    private val userStatsDao: UserStatsDao,
    private val remoteDataSource: UserStatsRemoteDataSource,
    private val sessionProvider: SessionProvider
) : StatsRepository {

    override fun getUserStats(): Flow<UserStats> =
        userStatsDao.observe(sessionProvider.getCurrentUserId())
            .map { entity -> entity?.toDomain() ?: UserStats() }

    override suspend fun refreshUserStats(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = sessionProvider.getCurrentUserId()
                if (userId.isBlank()) return@runCatching

                val remoteStats = remoteDataSource.getUserStats(userId) ?: return@runCatching
                userStatsDao.upsert(remoteStats.toEntity())
            }
        }

    override suspend fun addRecipeCookedStats(kg: Double, euros: Double, points: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = sessionProvider.getCurrentUserId()
                if (userId.isBlank()) return@runCatching

                val current = userStatsDao.getByUserId(userId)?.toDomain() ?: UserStats()
                val updated = UserStats(
                    totalKgSaved = current.totalKgSaved + kg,
                    totalEurosSaved = current.totalEurosSaved + euros,
                    totalEcoPoints = current.totalEcoPoints + points
                )

                val dto = updated.toDto(userId)
                val remoteResponse = remoteDataSource.upsertUserStats(dto)
                userStatsDao.upsert(remoteResponse.toEntity())
            }
        }

    override suspend fun spendEcoPoints(amount: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = sessionProvider.getCurrentUserId()
                require(userId.isNotBlank()) { "Utente non autenticato" }

                val current = userStatsDao.getByUserId(userId)?.toDomain() ?: UserStats()
                require(current.totalEcoPoints >= amount) { "Eco-Punti insufficienti" }

                val updated = current.copy(totalEcoPoints = current.totalEcoPoints - amount)
                val dto = updated.toDto(userId)
                val remoteResponse = remoteDataSource.upsertUserStats(dto)
                userStatsDao.upsert(remoteResponse.toEntity())
            }
        }

    // ===== HELPERS =====

    private fun UserStatsEntity.toDomain(): UserStats =
        UserStats(totalKgSaved = totalKgSaved, totalEurosSaved = totalEurosSaved, totalEcoPoints = totalEcoPoints)

    private fun UserStats.toDto(userId: String): UserStatsDto =
        UserStatsDto(
            userId = userId,
            totalKgSaved = totalKgSaved,
            totalEurosSaved = totalEurosSaved,
            totalEcoPoints = totalEcoPoints
        )

    private fun UserStatsDto.toEntity(): UserStatsEntity =
        UserStatsEntity(
            userId = userId,
            totalKgSaved = totalKgSaved,
            totalEurosSaved = totalEurosSaved,
            totalEcoPoints = totalEcoPoints
        )
}

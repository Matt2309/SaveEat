package com.mattiamularoni.saveeat.features.home.data.repository

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.home.data.local.HomeDao
import com.mattiamularoni.saveeat.features.home.data.mapper.HomeMapper
import com.mattiamularoni.saveeat.features.home.data.remote.HomeRemoteDataSource
import com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard
import com.mattiamularoni.saveeat.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementazione di HomeRepository con sincronizzazione Supabase e caching Room.
 *
 * Responsabilità:
 * - Orchestrare remote datasource + local cache
 * - Logica aggregazione dashboard (pantry, leaderboard, recipes)
 * - Offline-first pattern: fetch remoto → cache locale → query su Flow
 * - Gestione errori
 * - Operare su Dispatchers.IO (async, no Main Thread blocking)
 */
class HomeRepositoryImpl(
    private val homeDao: HomeDao,
    private val remoteDataSource: HomeRemoteDataSource,
    private val sessionProvider: SessionProvider
) : HomeRepository {

    /**
     * Osserva i dati completi della dashboard Home.
     *
     * Logica:
     * - Flow dalla cache Room per il dato principale
     * - Quando la flow emette, mapperà entity → domain model
     * - Non trigghera refresh automatico (manual refresh via refreshHomeDashboard())
     *
     * @return Flow<HomeDashboard> aggiornato quando cambiano dati
     */
    override fun observeHomeDashboard(): Flow<HomeDashboard> {
        return homeDao
            .observeHomeDashboard(sessionProvider.getCurrentUserId())
            .filterNotNull()
            .map { entity -> HomeMapper.entityToDomain(entity) }
    }

    /**
     * Sincronizza la dashboard con Supabase (refresh manuale).
     *
     * Logica:
     * 1. Fetch dati aggregati da remote datasource
     * 2. Mapperà DTO in entity Room
     * 3. Upsert in Room (REPLACE strategy)
     * 4. Ritorna true se successo
     *
     * Operazione: Dispatchers.IO (remote call + DB write)
     *
     * @return true se refresh riuscito, false se errore
     */
    override suspend fun refreshHomeDashboard(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val remoteDto = remoteDataSource.getHomeDashboard(sessionProvider.getCurrentUserId())
                val entity = HomeMapper.dtoToEntity(remoteDto, sessionProvider.getCurrentUserId())
                homeDao.insertOrUpdateDashboard(entity)
                true
            } catch (e: Exception) {
                // Log errore (future logging integration)
                // Per MVP: ritorna false senza rethrow per permettere fallback alla cache
                false
            }
        }

    /**
     * Recupera snapshot singolo della dashboard dalla cache.
     *
     * Legge una sola volta dalla cache Room locale (query sincrona).
     *
     * @return HomeDashboard se presente in cache, null altrimenti
     */
    override suspend fun getHomeDashboard(): HomeDashboard? =
        withContext(Dispatchers.IO) {
            try {
                homeDao.getHomeDashboard(sessionProvider.getCurrentUserId())?.let { entity ->
                    HomeMapper.entityToDomain(entity)
                }
            } catch (e: Exception) {
                throw Exception("Failed to get home dashboard: ${e.message}", e)
            }
        }
}

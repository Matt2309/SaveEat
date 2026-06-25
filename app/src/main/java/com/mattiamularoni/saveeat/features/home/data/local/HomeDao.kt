package com.mattiamularoni.saveeat.features.home.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO per le operazioni sulla dashboard Home in Room.
 *
 * Responsabilità:
 * - Query su home_dashboard table (get, insert, update, delete)
 * - Osservazione live con Flow per aggiornamenti real-time
 */
@Dao
interface HomeDao {
    /**
     * Recupera i dati aggregati della dashboard per un utente.
     *
     * @param userId UUID dell'utente
     * @return Flow che emette i dati dashboard quando cambiano
     */
    @Query("SELECT * FROM home_dashboard WHERE user_id = :userId LIMIT 1")
    fun observeHomeDashboard(userId: String): Flow<List<HomeDashboardEntity>>

    /**
     * Recupera i dati dashboard in modo sincrono (unica lettura).
     *
     * @param userId UUID dell'utente
     * @return entità dashboard se presente, null altrimenti
     */
    @Query("SELECT * FROM home_dashboard WHERE user_id = :userId LIMIT 1")
    suspend fun getHomeDashboard(userId: String): HomeDashboardEntity?

    /**
     * Inserisce o aggiorna i dati dashboard (replace strategy).
     *
     * @param dashboard entità da persistere
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDashboard(dashboard: HomeDashboardEntity)

    /**
     * Cancella i dati dashboard di un utente.
     *
     * @param userId UUID dell'utente
     * @return numero di righe cancellate
     */
    @Query("DELETE FROM home_dashboard WHERE user_id = :userId")
    suspend fun deleteDashboard(userId: String): Int
}

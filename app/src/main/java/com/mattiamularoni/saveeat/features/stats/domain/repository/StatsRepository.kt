package com.mattiamularoni.saveeat.features.stats.domain.repository

import com.mattiamularoni.saveeat.features.stats.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository per le statistiche di risparmio (kg e euro) dell'utente.
 *
 * Offline-first: i totali sono mantenuti in cache Room e sincronizzati con Supabase.
 */
interface StatsRepository {

    /**
     * Osserva i totali di risparmio dell'utente corrente.
     *
     * @return Flow che emette [UserStats], con valori a zero se non esiste ancora una riga.
     */
    fun getUserStats(): Flow<UserStats>

    /**
     * Incrementa i totali di risparmio dell'utente corrente con i valori di una ricetta cucinata.
     *
     * @param kg kg di cibo "salvati" stimati per la ricetta
     * @param euros euro stimati risparmiati per la ricetta
     * @return [Result] di successo, oppure fallimento con l'eccezione incontrata
     */
    suspend fun addSavings(kg: Double, euros: Double): Result<Unit>
}

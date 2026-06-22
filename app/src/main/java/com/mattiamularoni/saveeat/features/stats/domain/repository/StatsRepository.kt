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
     * Sincronizza la riga di statistiche dell'utente corrente da Supabase verso Room.
     *
     * Necessario perché [getUserStats] osserva solo la cache locale: senza questa chiamata
     * un utente che non ha mai cucinato una ricetta su questo device (es. dopo reinstall, o
     * perché i suoi punti sono stati assegnati prima che esistesse una riga locale) vedrebbe
     * sempre 0, anche se la riga remota ha valori validi.
     *
     * @return [Result] di successo, oppure fallimento con l'eccezione incontrata
     */
    suspend fun refreshUserStats(): Result<Unit>

    /**
     * Incrementa i totali dell'utente corrente (kg, euro, eco-punti) con i valori
     * di una ricetta cucinata. Unica fonte di verità per i traguardi utente.
     *
     * @param kg kg di cibo "salvati" stimati per la ricetta
     * @param euros euro stimati risparmiati per la ricetta
     * @param points eco-punti assegnati per la cucinata
     * @return [Result] di successo, oppure fallimento con l'eccezione incontrata
     */
    suspend fun addRecipeCookedStats(kg: Double, euros: Double, points: Int): Result<Unit>

    /**
     * Deduce [amount] eco-punti dal saldo dell'utente corrente, a fronte dello sblocco
     * di una feature premium (es. filtri avanzati di generazione ricette).
     *
     * @param amount eco-punti da spendere
     * @return [Result] di successo, oppure fallimento se l'utente non ha saldo sufficiente
     * o non è autenticato
     */
    suspend fun spendEcoPoints(amount: Int): Result<Unit>
}

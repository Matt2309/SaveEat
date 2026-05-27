package com.mattiamularoni.saveeat.core.data.remote

/**
 * Interface to provide current session user ID.
 *
 * Responsabilità:
 * - Fornire ID utente corrente
 * - Decoupling dall'Auth module (non ancora disponibile)
 * - Punto di integrazione per Auth quando disponibile
 *
 * Implementazione attuale: Mock con UUID di test.
 * Quando Auth module sarà pronto, sostituire MockSessionProvider con AuthSessionProvider
 * senza modificare codice nel repository o remote datasource.
 */
interface SessionProvider {
    /**
     * Restituisce UUID dell'utente corrente.
     *
     * @return UUID stringa dell'utente sessione attuale
     */
    fun getCurrentUserId(): String
}

/**
 * Mock implementation di SessionProvider per MVP.
 *
 * Usa UUID fisso di test. Sarà sostituito quando Auth module è pronto.
 */
object MockSessionProvider : SessionProvider {
    // UUID di test per MVP - verrà sostituito da Auth module
    private const val MOCK_USER_ID = "11111111-1111-1111-1111-111111111111"

    override fun getCurrentUserId(): String = MOCK_USER_ID
}

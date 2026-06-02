package com.mattiamularoni.saveeat.features.home.presentation.state

import com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard

/**
 * UI State sealed class per la home dashboard.
 *
 * Rappresenta i possibili stati della UI:
 * - Loading: Caricamento dati in corso
 * - Success: Dashboard caricata correttamente con dati
 * - Error: Errore nel caricamento (rete, parsing, etc.)
 * - Empty: Nessun dato disponibile (offline, primo accesso)
 *
 * Usato dal ViewModel per gestire la reattività della UI.
 */
sealed class HomeUiState {
    /**
     * Stato di caricamento.
     *
     * Tipicamente mostra uno skeleton loader o loading indicator.
     */
    data object Loading : HomeUiState()

    /**
     * Stato di successo con dati dashboard.
     *
     * @param dashboard dati aggregati della dashboard pronti per il rendering
     */
    data class Success(val dashboard: HomeDashboard) : HomeUiState()

    /**
     * Stato di errore.
     *
     * @param message messaggio di errore da mostrare all'utente
     */
    data class Error(val message: String) : HomeUiState()

    /**
     * Stato di cache vuota (offline, nessun dato precedente).
     *
     * Mostra UI placeholder suggerendo scan receipt o manual add.
     */
    data object Empty : HomeUiState()
}

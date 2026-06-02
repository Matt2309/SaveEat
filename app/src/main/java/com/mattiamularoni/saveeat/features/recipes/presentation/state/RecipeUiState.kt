package com.mattiamularoni.saveeat.features.recipes.presentation.state

import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe

/**
 * Modello dello stato UI per il modulo Recipes.
 *
 * Rappresenta i possibili stati dell'interfaccia durante le operazioni
 * di caricamento, errore e visualizzazione ricette.
 */
sealed class RecipeUiState {
    /**
     * Stato di caricamento iniziale.
     *
     * Emesso quando la ricerca ricette è in corso.
     */
    object Loading : RecipeUiState()

    /**
     * Stato di successo con lista ricette.
     *
     * @param recipes lista di ricette caricate con successo
     * @param isRefreshing indica se il refresh è in corso (per pull-to-refresh)
     */
    data class Success(
        val recipes: List<Recipe> = emptyList(),
        val isRefreshing: Boolean = false
    ) : RecipeUiState()

    /**
     * Stato di errore con messaggio.
     *
     * @param message descrizione dell'errore per l'utente
     * @param exception eccezione originale per debugging
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : RecipeUiState()

    /**
     * Stato di lista vuota (nessuna ricetta trovata).
     */
    object Empty : RecipeUiState()
}

/**
 * Modello dello stato UI per il modulo Favorite Recipes.
 *
 * Rappresenta i possibili stati dell'interfaccia durante le operazioni
 * su ricette preferite.
 */
sealed class FavoriteRecipeUiState {
    /**
     * Stato di caricamento iniziale.
     */
    object Loading : FavoriteRecipeUiState()

    /**
     * Stato di successo con lista ricette preferite.
     *
     * @param recipes lista di ricette preferite
     */
    data class Success(
        val recipes: List<Recipe> = emptyList()
    ) : FavoriteRecipeUiState()

    /**
     * Stato di errore.
     *
     * @param message descrizione dell'errore
     * @param exception eccezione originale
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : FavoriteRecipeUiState()

    /**
     * Stato di lista vuota.
     */
    object Empty : FavoriteRecipeUiState()
}

/**
 * Modello dello stato UI per la generazione ricette intelligenti.
 *
     */
sealed class GenerateRecipeUiState {
    /**
     * Stato iniziale (nessuna generazione in corso).
     */
    object Idle : GenerateRecipeUiState()

    /**
     * Stato di generazione in corso (AI sta elaborando).
     *
     * @param progress percentuale di avanzamento (0-100)
     */
    data class Generating(
        val progress: Int = 0
    ) : GenerateRecipeUiState()

    /**
     * Stato di successo con ricette generate.
     *
     * @param recipes lista di ricette generate dall'AI
     */
    data class Success(
        val recipes: List<Recipe> = emptyList()
    ) : GenerateRecipeUiState()

    /**
     * Stato di errore durante la generazione.
     *
     * @param message descrizione dell'errore
     * @param exception eccezione originale
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : GenerateRecipeUiState()
}

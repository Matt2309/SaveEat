package com.mattiamularoni.saveeat.features.recipes.presentation.state

/**
 * Eventi one-shot del modulo Recipes, da consumare una sola volta lato UI
 * (es. snackbar, navigazione) senza essere ri-emessi in caso di recomposition.
 */
sealed class RecipeUiEvent {
    /**
     * La ricetta è stata segnata come cucinata con successo: ingredienti dedotti
     * dalla dispensa ed eco-punti assegnati.
     *
     * @param pointsAwarded eco-punti assegnati per l'azione
     */
    data class CookSuccess(val pointsAwarded: Int) : RecipeUiEvent()

    /**
     * Errore durante l'azione "segna come cucinata".
     *
     * @param message descrizione dell'errore per l'utente
     */
    data class CookError(val message: String) : RecipeUiEvent()
}

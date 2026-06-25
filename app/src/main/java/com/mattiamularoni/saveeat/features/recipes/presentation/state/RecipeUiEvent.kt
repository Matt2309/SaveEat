package com.mattiamularoni.saveeat.features.recipes.presentation.state

import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem

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
    data class CookSuccess(
        val pointsAwarded: Int,
    ) : RecipeUiEvent()

    /**
     * Un ingrediente è stato aggiunto alla lista della spesa locale.
     *
     * @param items lista della spesa aggiornata, da passare all'app Note
     */
    data class AddedToShoppingList(
        val items: List<ShoppingListItem>,
    ) : RecipeUiEvent()

    /**
     * Errore durante l'azione "segna come cucinata".
     *
     * @param message descrizione dell'errore per l'utente
     */
    data class CookError(
        val message: String,
    ) : RecipeUiEvent()

    /**
     * Sblocco dei filtri avanzati di generazione ricette fallito (eco-punti insufficienti
     * o utente non autenticato).
     */
    data object PremiumUnlockFailed : RecipeUiEvent()

    /**
     * La ricetta è stata eliminata con successo (locale + remoto).
     */
    data object RecipeDeleted : RecipeUiEvent()

    /**
     * Errore durante l'eliminazione della ricetta.
     *
     * @param message descrizione dell'errore per l'utente
     */
    data class DeleteError(
        val message: String,
    ) : RecipeUiEvent()
}

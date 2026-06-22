package com.mattiamularoni.saveeat.features.recipes.domain.model

import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe

/**
 * Filtro applicabile alla lista ricette, basato su un tag effettivamente presente
 * nelle ricette (es. "fusion", "croccante", "antipasto").
 *
 * La lista dei filtri disponibili non è fissa: viene derivata dai tag distinti delle
 * ricette caricate (vedi [com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel.availableFilters]),
 * così ogni badge mostrato su una ricetta corrisponde sempre a un filtro selezionabile.
 */
data class RecipeFilter(val tag: String) {

    /** Etichetta del chip, con iniziale maiuscola (es. "fusion" -> "Fusion"). */
    val label: String
        get() = tag.replaceFirstChar { it.uppercase() }

    /**
     * Verifica se [recipe] possiede questo tag (case-insensitive).
     */
    fun matches(recipe: Recipe): Boolean =
        recipe.tags.any { it.trim().equals(tag, ignoreCase = true) }
}

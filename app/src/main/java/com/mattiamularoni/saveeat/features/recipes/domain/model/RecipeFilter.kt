package com.mattiamularoni.saveeat.features.recipes.domain.model

import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe

/**
 * Filtro applicabile alla lista ricette.
 *
 * Ogni filtro sa descriversi (label) e verificare se una ricetta lo soddisfa
 * (matches). I filtri sono raggruppati per categoria (Style, Time, Vegetarian):
 * la logica di combinazione (OR nella categoria, AND fra categorie) vive in
 * [com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel].
 */
sealed interface RecipeFilter {
    val label: String

    /**
     * Verifica se [recipe] soddisfa questo filtro.
     */
    fun matches(recipe: Recipe): Boolean

    /**
     * Filtro per stile culinario. Lo stile non è un campo strutturato della
     * ricetta: viene dedotto controllando se i [Recipe.tags] contengono la
     * sottostringa associata (tollera forme maschili/femminili, es. "italiano"/"italiana").
     */
    enum class Style(override val label: String, private val tag: String) : RecipeFilter {
        ITALIANA("Italiana", "italian"),
        ASIATICA("Asiatica", "asiat"),
        MESSICANA("Messicana", "messic");

        override fun matches(recipe: Recipe): Boolean =
            recipe.tags.any { it.contains(tag, ignoreCase = true) }
    }

    /**
     * Filtro per tempo di preparazione, in fasce di minuti.
     */
    enum class Time(override val label: String, private val range: IntRange) : RecipeFilter {
        VELOCE("Veloce", 0..19),
        MEDIO("Medio", 20..40),
        LUNGO("Lungo", 41..Int.MAX_VALUE);

        override fun matches(recipe: Recipe): Boolean = recipe.prepTimeMinutes in range
    }

    /**
     * Filtro per ricette vegetariane.
     */
    data object Vegetarian : RecipeFilter {
        override val label: String = "Vegetariano"
        override fun matches(recipe: Recipe): Boolean = recipe.isVegetarian
    }

    companion object {
        /** Tutte le opzioni di filtro disponibili nella UI, in ordine di visualizzazione. */
        val all: List<RecipeFilter> = Style.entries + Time.entries + Vegetarian
    }
}

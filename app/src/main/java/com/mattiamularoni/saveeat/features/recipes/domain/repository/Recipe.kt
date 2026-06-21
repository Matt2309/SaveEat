package com.mattiamularoni.saveeat.features.recipes.domain.repository

/**
 * Modello di dominio per una ricetta.
 *
 * Rappresenta una ricetta disponibile nell'app con tutti i metadati
 * necessari per visualizzazione, filtraggio e matching con ingredienti.
 */
data class Recipe(
    val id: String,
    val title: String,
    val instructions: String,
    val ingredients: List<Ingredient>,
    val prepTimeMinutes: Int,
    val tags: List<String>,
    val createdAt: Long,
    val isVegetarian: Boolean = false,
    val estimatedWeightKg: Double = 0.0,
    val estimatedCostEuros: Double = 0.0
) {
    /**
     * Modello per un singolo ingrediente di una ricetta.
     *
     * @param name nome dell'ingrediente
     * @param amount quantità richiesta
     * @param unit unità di misura (g, ml, cucchiai, ecc.)
     */
    data class Ingredient(
        val name: String,
        val amount: Double,
        val unit: String
    )
}

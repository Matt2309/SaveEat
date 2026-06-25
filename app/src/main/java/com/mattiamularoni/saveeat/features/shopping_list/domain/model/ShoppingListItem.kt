package com.mattiamularoni.saveeat.features.shopping_list.domain.model

/**
 * Modello di dominio per un elemento della lista della spesa locale.
 */
data class ShoppingListItem(
    val id: String,
    val name: String,
    val addedAt: Long,
)

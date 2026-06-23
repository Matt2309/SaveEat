package com.mattiamularoni.saveeat.features.shopping_list.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entità Room per un elemento della lista della spesa.
 *
 * `normalizedName` (lowercase + trim) ha un indice UNIQUE: la deduplicazione
 * case-insensitive per nome è garantita dal DB stesso (insert con
 * OnConflictStrategy.IGNORE), non da un controllo "leggi-poi-scrivi" in memoria.
 */
@Entity(
    tableName = "shopping_list_items",
    indices = [Index(value = ["normalizedName"], unique = true)]
)
data class ShoppingListItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val normalizedName: String,
    val addedAt: Long
)

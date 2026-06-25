package com.mattiamularoni.saveeat.features.shopping_list.domain.repository

import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository per la lista della spesa, persistita solo localmente (Room),
 * senza sincronizzazione remota.
 */
interface ShoppingListRepository {
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    suspend fun addItem(name: String): Result<Unit>

    suspend fun removeItem(id: String): Result<Unit>

    suspend fun clearList(): Result<Unit>
}

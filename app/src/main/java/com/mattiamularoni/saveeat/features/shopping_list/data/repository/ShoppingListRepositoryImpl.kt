package com.mattiamularoni.saveeat.features.shopping_list.data.repository

import com.mattiamularoni.saveeat.features.shopping_list.data.local.ShoppingListDao
import com.mattiamularoni.saveeat.features.shopping_list.data.local.ShoppingListItemEntity
import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem
import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ShoppingListRepositoryImpl(
    private val dao: ShoppingListDao
) : ShoppingListRepository {

    override fun getShoppingList(): Flow<List<ShoppingListItem>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addItem(name: String): Result<Unit> = runCatching {
        dao.insert(
            ShoppingListItemEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                normalizedName = name.trim().lowercase(),
                addedAt = System.currentTimeMillis()
            )
        )
    }.map { }

    override suspend fun removeItem(id: String): Result<Unit> = runCatching {
        dao.deleteById(id)
    }.map { }

    override suspend fun clearList(): Result<Unit> = runCatching {
        dao.clear()
    }.map { }

    private fun ShoppingListItemEntity.toDomain() = ShoppingListItem(
        id = id,
        name = name,
        addedAt = addedAt
    )
}

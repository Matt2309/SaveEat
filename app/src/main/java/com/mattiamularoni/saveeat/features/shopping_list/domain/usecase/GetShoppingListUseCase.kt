package com.mattiamularoni.saveeat.features.shopping_list.domain.usecase

import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem
import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(): Flow<List<ShoppingListItem>> = repository.getShoppingList()
}

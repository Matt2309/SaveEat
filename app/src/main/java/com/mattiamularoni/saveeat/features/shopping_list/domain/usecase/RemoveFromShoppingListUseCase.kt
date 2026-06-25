package com.mattiamularoni.saveeat.features.shopping_list.domain.usecase

import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository

class RemoveFromShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.removeItem(id)
}

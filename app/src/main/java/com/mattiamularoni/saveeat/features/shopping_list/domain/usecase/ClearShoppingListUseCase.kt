package com.mattiamularoni.saveeat.features.shopping_list.domain.usecase

import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository

class ClearShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.clearList()
}

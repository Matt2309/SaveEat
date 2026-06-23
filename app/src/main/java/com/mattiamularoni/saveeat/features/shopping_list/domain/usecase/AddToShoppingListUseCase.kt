package com.mattiamularoni.saveeat.features.shopping_list.domain.usecase

import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository

/**
 * Aggiunge un ingrediente alla lista della spesa locale.
 *
 * La deduplicazione case-insensitive per nome è garantita a livello DB
 * (indice UNIQUE su `normalizedName` + insert IGNORE), non da un controllo
 * qui: evita la race condition di un "leggi-poi-scrivi" su due tap rapidi.
 */
class AddToShoppingListUseCase(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(name: String): Result<Unit> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return Result.success(Unit)
        return repository.addItem(trimmed)
    }
}

package com.mattiamularoni.saveeat.features.notifications.domain.usecase

import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository

class MarkItemsNotifiedUseCase(
    private val repository: PantryRepository,
) {
    suspend operator fun invoke(ids: List<String>) = repository.markItemsNotified(ids)
}

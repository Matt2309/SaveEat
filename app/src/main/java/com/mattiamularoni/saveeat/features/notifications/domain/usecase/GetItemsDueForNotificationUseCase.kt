package com.mattiamularoni.saveeat.features.notifications.domain.usecase

import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository

class GetItemsDueForNotificationUseCase(private val repository: PantryRepository) {
    suspend operator fun invoke(windowEnd: Long): List<PantryItem> =
        repository.getItemsDueForNotification(windowEnd)
}

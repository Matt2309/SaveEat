package com.mattiamularoni.saveeat.features.pantry.presentation.domain

import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem as DomainPantryItem
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.FreshnessLevel
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPantryItemsUseCase(
    private val pantryRepository: PantryRepository
) {
    operator fun invoke(): Flow<List<PantryItem>> {
        return pantryRepository.observePantryItems().map { items ->
            items.map { item -> item.toUiModel() }
        }
    }

    private fun DomainPantryItem.toUiModel(): PantryItem {
        val category = resolveCategory(name)
        val freshnessLevel = resolveFreshness(expiresAt)

        return PantryItem(
            id = id,
            name = name,
            quantity = formatQuantity(quantity, unit),
            expirationLabel = formatExpirationLabel(expiresAt),
            freshnessLevel = freshnessLevel,
            imageUrl = null,
            category = category,
            isPlaceholder = false
        )
    }
}

private fun resolveCategory(name: String): PantryCategory {
    val normalized = name.lowercase()
    return when {
        listOf("milk", "latte", "yogurt", "burro", "butter", "cheese", "formaggio", "egg", "uova", "cream").any { normalized.contains(it) } ->
            PantryCategory.FRIDGE
        listOf("frozen", "freezer", "gelato", "ice", "surgel", "spinaci", "peas").any { normalized.contains(it) } ->
            PantryCategory.FREEZER
        else -> PantryCategory.PANTRY
    }
}

private fun resolveFreshness(expiresAt: Long?): FreshnessLevel {
    if (expiresAt == null) return FreshnessLevel.HIGH

    val daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), Instant.ofEpochMilli(expiresAt))
    return when {
        daysUntilExpiry <= 1 -> FreshnessLevel.CRITICAL
        daysUntilExpiry <= 5 -> FreshnessLevel.MEDIUM
        else -> FreshnessLevel.HIGH
    }
}

private fun formatExpirationLabel(expiresAt: Long?): String {
    if (expiresAt == null) return "Lunga conservazione"

    val daysUntilExpiry = Duration.between(Instant.now(), Instant.ofEpochMilli(expiresAt)).toDays()
    return when {
        daysUntilExpiry < 0 -> "Scaduto"
        daysUntilExpiry == 0L -> "Scade oggi!"
        daysUntilExpiry == 1L -> "Scade domani"
        else -> "Scade in $daysUntilExpiry giorni"
    }
}

private fun formatQuantity(quantity: Double, unit: String?): String {
    val formattedQuantity = if (quantity % 1.0 == 0.0) {
        quantity.toLong().toString()
    } else {
        quantity.toString().trimEnd('0').trimEnd('.')
    }
    return if (unit.isNullOrBlank()) formattedQuantity else "$formattedQuantity $unit"
}

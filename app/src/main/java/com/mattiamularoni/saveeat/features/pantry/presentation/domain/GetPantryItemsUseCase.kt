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
        val freshnessLevel = resolveFreshness(expirationDate)

        return PantryItem(
            id = id,
            name = name,
            quantity = formatQuantity(quantity, unit),
            expirationLabel = formatExpirationLabel(expirationDate),
            freshnessLevel = freshnessLevel,
            imageUrl = null,
            category = mapDomainCategory(category),
            isPlaceholder = isPlaceholder,
            categoryKey = categoryKey
        )
    }

    /**
     * Mappa il valore stringa category dal domain al PantryCategory enum.
     * Fallback a PANTRY se categoria non riconosciuta.
     */
    private fun mapDomainCategory(categoryString: String): PantryCategory {
        return when (categoryString.uppercase()) {
            "FRIDGE" -> PantryCategory.FRIDGE
            "FREEZER" -> PantryCategory.FREEZER
            "PANTRY" -> PantryCategory.PANTRY
            else -> PantryCategory.PANTRY
        }
    }
}

private fun resolveFreshness(expirationDate: Long?): FreshnessLevel {
    if (expirationDate == null) return FreshnessLevel.HIGH

    val daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), Instant.ofEpochMilli(expirationDate))
    return when {
        daysUntilExpiry <= 1 -> FreshnessLevel.CRITICAL
        daysUntilExpiry <= 5 -> FreshnessLevel.MEDIUM
        else -> FreshnessLevel.HIGH
    }
}

private fun formatExpirationLabel(expirationDate: Long?): String {
    if (expirationDate == null) return "Lunga conservazione"

    val daysUntilExpiry = Duration.between(Instant.now(), Instant.ofEpochMilli(expirationDate)).toDays()
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


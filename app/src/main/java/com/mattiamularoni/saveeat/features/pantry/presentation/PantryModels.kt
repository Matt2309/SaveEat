package com.mattiamularoni.saveeat.features.pantry.presentation

enum class PantryCategory {
    ALL,
    FRIDGE,
    PANTRY,
    FREEZER
}

enum class FreshnessLevel {
    HIGH,
    MEDIUM,
    CRITICAL
}

data class PantryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val expirationLabel: String,
    val freshnessLevel: FreshnessLevel,
    val imageUrl: String?,
    val category: PantryCategory,
    val isPlaceholder: Boolean,
    val placeholderIcon: String = "inventory_2"
)

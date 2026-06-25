package com.mattiamularoni.saveeat.features.pantry.domain.model

data class PantryAsset(
    val categoryKey: String,
    val names: Map<String, String>,
    val imageUrl: String?,
)

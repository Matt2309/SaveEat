package com.mattiamularoni.saveeat.features.pantry.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PantryAssetDto(
    @SerialName("category_key") val categoryKey: String,
    val names: Map<String, String>,
    @SerialName("image_url") val imageUrl: String? = null,
)

package com.mattiamularoni.saveeat.features.recipes.data.remote.pixabay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO minimale per la risposta della Pixabay Image Search API.
 * Estrae solo i campi necessari per recuperare l'URL della prima immagine.
 */
@Serializable
data class PixabayResponseDto(
    val hits: List<PixabayHitDto> = emptyList()
)

@Serializable
data class PixabayHitDto(
    @SerialName("webformatURL")
    val webformatUrl: String? = null
)

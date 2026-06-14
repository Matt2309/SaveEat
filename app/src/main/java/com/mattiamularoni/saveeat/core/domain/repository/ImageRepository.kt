package com.mattiamularoni.saveeat.core.domain.repository

interface ImageRepository {
    suspend fun getImageUrl(query: String): String?
}

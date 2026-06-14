package com.mattiamularoni.saveeat.core.domain.repository

interface TranslationRepository {
    suspend fun translateToEnglish(text: String): String
}

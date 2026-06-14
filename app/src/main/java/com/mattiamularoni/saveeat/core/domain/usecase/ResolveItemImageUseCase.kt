package com.mattiamularoni.saveeat.core.domain.usecase

import com.mattiamularoni.saveeat.core.domain.repository.ImageRepository
import com.mattiamularoni.saveeat.core.domain.repository.TranslationRepository

class ResolveItemImageUseCase(
    private val translationRepository: TranslationRepository,
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(label: String): String? {
        return try {
            val englishLabel = translationRepository.translateToEnglish(label)
            imageRepository.getImageUrl(englishLabel)
        } catch (e: Exception) {
            null
        }
    }
}

package com.mattiamularoni.saveeat.core.data.repository

import com.mattiamularoni.saveeat.core.data.remote.TranslationRemoteDataSource
import com.mattiamularoni.saveeat.core.domain.repository.TranslationRepository

class TranslationRepositoryImpl(
    private val remoteDataSource: TranslationRemoteDataSource
) : TranslationRepository {
    override suspend fun translateToEnglish(text: String): String =
        remoteDataSource.translate(text)
}

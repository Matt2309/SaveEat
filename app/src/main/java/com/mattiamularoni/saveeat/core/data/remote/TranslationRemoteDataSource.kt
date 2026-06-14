package com.mattiamularoni.saveeat.core.data.remote

interface TranslationRemoteDataSource {
    suspend fun translate(text: String): String
}

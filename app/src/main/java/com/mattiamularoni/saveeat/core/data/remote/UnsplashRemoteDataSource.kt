package com.mattiamularoni.saveeat.core.data.remote

interface UnsplashRemoteDataSource {
    suspend fun fetchImageUrl(query: String): String?
}

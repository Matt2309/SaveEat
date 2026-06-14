package com.mattiamularoni.saveeat.core.data.repository

import com.mattiamularoni.saveeat.core.data.remote.UnsplashRemoteDataSource
import com.mattiamularoni.saveeat.core.domain.repository.ImageRepository

class ImageRepositoryImpl(
    private val remoteDataSource: UnsplashRemoteDataSource
) : ImageRepository {
    override suspend fun getImageUrl(query: String): String? =
        remoteDataSource.fetchImageUrl(query)
}

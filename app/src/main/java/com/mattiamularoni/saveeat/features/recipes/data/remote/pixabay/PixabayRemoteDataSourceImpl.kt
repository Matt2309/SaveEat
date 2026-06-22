package com.mattiamularoni.saveeat.features.recipes.data.remote.pixabay

import com.mattiamularoni.saveeat.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PixabayRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : PixabayRemoteDataSource {

    override suspend fun fetchImageUrl(query: String): String? =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.PIXABAY_API_KEY
            if (query.isBlank() || apiKey.isBlank() || apiKey == "null") {
                return@withContext null
            }

            try {
                val response = httpClient.get("https://pixabay.com/api/") {
                    parameter("key", apiKey)
                    parameter("q", query)
                    parameter("image_type", "photo")
                    parameter("category", "food")
                    parameter("per_page", 3)
                    parameter("safesearch", true)
                }.body<PixabayResponseDto>()

                response.hits.firstOrNull()?.webformatUrl
            } catch (e: Exception) {
                // Un fallimento di Pixabay non deve mai bloccare la generazione delle ricette:
                // si ricade semplicemente su nessuna immagine.
                android.util.Log.w("PixabayRemoteDataSource", "Failed to fetch image for query '$query'", e)
                null
            }
        }
}

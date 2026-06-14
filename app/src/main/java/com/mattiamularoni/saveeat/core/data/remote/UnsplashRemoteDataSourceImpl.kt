package com.mattiamularoni.saveeat.core.data.remote

import com.mattiamularoni.saveeat.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class UnsplashRemoteDataSourceImpl : UnsplashRemoteDataSource {

    private val client = HttpClient(Android)

    override suspend fun fetchImageUrl(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.unsplash.com/search/photos?query=$encoded&per_page=1&client_id=${BuildConfig.UNSPLASH_ACCESS_KEY}"
            val body = client.get(url).bodyAsText()
            val results = JSONObject(body).getJSONArray("results")
            if (results.length() == 0) return@withContext null
            results.getJSONObject(0).getJSONObject("urls").getString("small")
        } catch (e: Exception) {
            null
        }
    }
}

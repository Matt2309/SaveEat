package com.mattiamularoni.saveeat.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class TranslationRemoteDataSourceImpl : TranslationRemoteDataSource {

    private val client = HttpClient(Android)

    override suspend fun translate(text: String): String = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=autodetect|en"
            val body = client.get(url).bodyAsText()
            JSONObject(body).getJSONObject("responseData").getString("translatedText")
        } catch (e: Exception) {
            text
        }
    }
}

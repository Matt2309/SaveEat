package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.mattiamularoni.saveeat.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiReceiptDataSourceImpl : GeminiReceiptDataSource {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun analyzeReceipt(bitmap: Bitmap): String = withContext(Dispatchers.IO) {

        // IL PROMPT: L'istruzione per domare l'AI
        val prompt = """
            Sei un assistente per un'app di gestione della dispensa.
            Analizza questa immagine di uno scontrino della spesa ed estrai i prodotti alimentari.
            Devi restituire ESCLUSIVAMENTE un array JSON valido, senza blocchi di codice markdown (niente ```json), senza saluti o testo aggiuntivo.
            Usa ESATTAMENTE questa struttura per ogni prodotto:
            [
              {
                "name": "Nome pulito del prodotto (es. Latte Parzialmente Scremato)",
                "category": "FRIDGE" | "PANTRY" | "FREEZER",
                "quantity": 1.0,
                "unit": "pz" | "kg" | "l" | "g" | "ml"
              }
            ]
            Regole:
            - Dedurre la categoria corretta: "FRIDGE" (frigo), "FREEZER" (surgelati), "PANTRY" (scaffale/dispensa).
            - Se la quantità non è chiara, metti 1.0.
            - Se l'unità non è chiara, metti "pz" (pezzi).
            - Ignora le tasse, il totale, gli sconti o i prodotti non alimentari (es. detersivi, sacchetti).
        """.trimIndent()

        try {
            // Chiamata ai server di Google
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val jsonText = response.text

            if (jsonText.isNullOrBlank()) {
                throw Exception("L'AI ha restituito una risposta vuota.")
            }

            // Pulizia di sicurezza nel caso Gemini inserisca i backtick del markdown
            return@withContext jsonText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

        } catch (e: Exception) {
            throw Exception("Errore durante l'analisi dello scontrino: ${e.message}", e)
        }
    }
}
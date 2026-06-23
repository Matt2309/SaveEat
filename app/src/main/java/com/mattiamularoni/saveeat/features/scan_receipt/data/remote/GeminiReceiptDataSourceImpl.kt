package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage

import dev.shreyaspatil.ai.client.generativeai.type.generationConfig

class GeminiReceiptDataSourceImpl : GeminiReceiptDataSource {
    // Configuriamo il modello forzando la risposta come JSON puro
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    override suspend fun analyzeReceipt(bitmap: Bitmap): String = withContext(Dispatchers.IO) {

        // 1. Convertiamo la Bitmap di Android in un ByteArray (necessario per la lib KMP)
        val stream = ByteArrayOutputStream()
        // Uso JPEG con 90% di qualità: ottimo compromesso tra peso in rete e leggibilità dello scontrino
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageByteArray = stream.toByteArray()

        val prompt = """
            Analizza questa immagine di uno scontrino della spesa ed estrai il negozio, il totale pagato e i prodotti alimentari.
            Non inventarti nomi diversi da quelli che leggi nello scontrino.
            Restituisci un JSON valido seguendo ESATTAMENTE questo schema:
            {
              "store_name": "Nome del negozio (es. Conad, Esselunga)",
              "total_price": 45.20,
              "items": [
                {
                  "name": "Nome pulito del prodotto (es. Latte Parzialmente Scremato)",
                  "category_key": "chiave_categoria_standard_minuscolo",
                  "category": "FRIDGE" | "PANTRY" | "FREEZER",
                  "quantity": 1.0,
                  "unit": "pz" | "kg" | "l" | "g" | "ml",
                  "is_perishable": true,
                  "estimated_expiry_days": 3
                }
              ]
            }
            Regole per category_key:
            - DEVI assegnare una category_key standard in snake_case minuscolo a ogni prodotto (es. "pomodoro", "latte_intero", "pollo", "uova", "pasta_spaghetti").
            - La chiave deve essere in snake_case senza spazi o caratteri speciali e al singolare (es. carote NO -> carota SI).
            - Se il prodotto è completamente sconosciuto o non classificabile, usa "generic_food".
            Regole generali:
            - Dedurre la categoria corretta: "FRIDGE" (frigo), "FREEZER" (surgelati), "PANTRY" (scaffale/dispensa).
            - "is_perishable": metti true SOLO per prodotti freschi/deperibili che vanno tenuti in frigo
              o consumati in pochi giorni (carne, pesce, latticini freschi, verdura e frutta fresca).
              Metti false per i prodotti a lunga conservazione (pasta, scatolame, surgelati, bibite,
              snack confezionati, prodotti da dispensa).
            - "estimated_expiry_days": giorni di conservazione stimati come numero INTERO
              (es. 3 per pollo fresco, 4 per insalata, 7 per yogurt, 365 per pasta secca o scatolame).
            - Se la quantità non è chiara, metti 1.0.
            - Se l'unità non è chiara, metti "pz" (pezzi).
            - Se il nome del negozio non è leggibile, usa "Scontrino".
            - Il total_price è il totale finale pagato (TOTALE), non i singoli prezzi degli articoli.
            - Negli "items" ignora le tasse, il totale, gli sconti o i prodotti non alimentari (es. detersivi, sacchetti).
        """.trimIndent()

        try {
            // Chiamata ai server di Google
            val response = generativeModel.generateContent(
                content {
                    // 2. Usiamo PlatformImage() passando il ByteArray
                    image(PlatformImage(imageByteArray))
                    text(prompt)
                }
            )

            val jsonText = response.text

            if (BuildConfig.DEBUG) {
                android.util.Log.d("OUTPUT_GEMINI", "$jsonText")
            }

            if (jsonText.isNullOrBlank()) {
                throw Exception("L'AI ha restituito una risposta vuota.")
            }

            return@withContext jsonText.trim()

        } catch (e: Exception) {
            throw Exception("Errore durante l'analisi dello scontrino: ${e.message}", e)
        }
    }
}
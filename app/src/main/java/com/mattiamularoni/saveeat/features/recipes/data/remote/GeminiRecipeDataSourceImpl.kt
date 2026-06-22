package com.mattiamularoni.saveeat.features.recipes.data.remote

import com.mattiamularoni.saveeat.BuildConfig
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRecipeDataSourceImpl : GeminiRecipeDataSource {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.1-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    override suspend fun generateRecipes(
        ingredients: List<String>,
        preferences: Map<String, Any>
    ): String = withContext(Dispatchers.IO) {
        val ingredientsList = if (ingredients.isNotEmpty())
            ingredients.joinToString(", ")
        else
            "ingredienti generici della dispensa"

        val cuisineHint = preferences["cuisine_style"]?.let {
            "\nStile culinario preferito: $it."
        } ?: ""

        val timingHint = when (preferences["timing"]) {
            "veloce" -> "\nTempo massimo di preparazione: 15 minuti."
            "medio" -> "\nTempo massimo di preparazione: 30 minuti."
            "lungo" -> "\nTempo massimo di preparazione: 60 minuti."
            else -> ""
        }

        val maxMinutes = when (preferences["timing"]) {
            "veloce" -> 15
            "medio" -> 30
            "lungo" -> 60
            else -> 60
        }

        val vegetarianHint = if (preferences["vegetarian"] == true) {
            "\nLe ricette devono essere vegetariane: nessuna carne né pesce."
        } else ""

        val prompt = """
            Genera 3 ricette creative usando PRINCIPALMENTE questi ingredienti in scadenza: $ingredientsList.$cuisineHint$timingHint$vegetarianHint
            Le ricette devono avere un tempo di preparazione massimo di $maxMinutes minuti.
            Restituisci un array JSON valido con ESATTAMENTE questo schema:
            [
              {
                "title": "Nome della ricetta",
                "instructions": "Istruzioni di preparazione dettagliate passo per passo",
                "ingredients": [
                  {"name": "nome ingrediente", "amount": 1.0, "unit": "g|ml|pz|cucchiai|qb"}
                ],
                "prep_time_minutes": 20,
                "tags": ["tag1", "tag2"],
                "is_vegetarian": false,
                "estimated_weight_kg": 0.6,
                "estimated_cost_euros": 4.20
              }
            ]
            Regole:
            - Usa PRINCIPALMENTE gli ingredienti forniti, aggiungendo solo ingredienti base comuni (sale, olio, ecc.)
            - Le istruzioni devono essere chiare e in italiano, separate dal carattere di newline escapato (\n), SENZA numerazione né elenchi (niente "1.", "2)", trattini): la UI numera già i passi
            - I tag devono essere singole parole brevi che descrivono la ricetta (es. "veloce", "vegetariano", "italiano", "pasta"), senza virgolette, virgole o punteggiatura al loro interno
            - "is_vegetarian" deve essere true SOLO se la ricetta non contiene carne né pesce, false altrimenti
            - "estimated_weight_kg" è il peso TOTALE commestibile della ricetta finita in kg (numero decimale realistico, es. 0.6)
            - "estimated_cost_euros" è il costo TOTALE stimato degli ingredienti in euro al mercato italiano (numero decimale realistico, es. 4.20)
            - Il tempo di preparazione deve essere realistico e non superare $maxMinutes minuti
            - Genera esattamente 3 ricette diverse
            - Rispondi SOLO con l'array JSON richiesto, senza testo aggiuntivo né blocchi di codice markdown (niente ```)
            - Il JSON deve essere sintatticamente valido: usa solo virgolette doppie, nessuna virgola finale, ed effettua l'escape di eventuali virgolette o ritorni a capo letterali contenuti nei valori stringa
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(
                content { text(prompt) }
            )

            val jsonText = response.text
            android.util.Log.d("OUTPUT_GEMINI_RECIPE", "$jsonText")

            if (jsonText.isNullOrBlank()) {
                throw Exception("Gemini ha restituito una risposta vuota.")
            }

            extractJsonArray(stripMarkdownFences(jsonText))
        } catch (e: Exception) {
            throw Exception("Errore durante la generazione delle ricette: ${e.message}", e)
        }
    }

    /**
     * Rimuove eventuali blocchi di codice markdown (```json ... ``` o ``` ... ```)
     * che il modello potrebbe aggiungere attorno al JSON nonostante le istruzioni.
     */
    private fun stripMarkdownFences(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    /**
     * Isola l'array JSON di primo livello, scartando qualunque testo che il modello
     * abbia aggiunto dopo la `]` di chiusura (es. commenti, spiegazioni, ricette
     * troncate): senza questo, kotlinx.serialization fallisce con "Expected EOF
     * after parsing" non appena Gemini ignora l'istruzione "nessun testo aggiuntivo".
     */
    private fun extractJsonArray(text: String): String {
        val start = text.indexOf('[')
        if (start == -1) return text

        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val c = text[i]
            when {
                inString -> when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '"' -> inString = false
                }
                c == '"' -> inString = true
                c == '[' -> depth++
                c == ']' -> {
                    depth--
                    if (depth == 0) return text.substring(start, i + 1)
                }
            }
        }
        return text.substring(start)
    }
}

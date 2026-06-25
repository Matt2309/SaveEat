package com.mattiamularoni.saveeat.features.recipes.data.remote.pixabay

/**
 * Data source per la ricerca di immagini food su Pixabay.
 *
 * Usato per associare una foto realistica alle ricette generate da Gemini,
 * evitando la generazione AI di immagini (lenta e costosa).
 */
interface PixabayRemoteDataSource {
    /**
     * Cerca un'immagine su Pixabay per la query fornita.
     *
     * @param query query di ricerca (1-2 parole, in inglese)
     * @return l'URL della prima immagine trovata, oppure null se la ricerca
     * non produce risultati o fallisce per qualsiasi motivo (rete, parsing, ecc.)
     */
    suspend fun fetchImageUrl(query: String): String?
}

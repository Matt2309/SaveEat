package com.mattiamularoni.saveeat.features.pantry.data.mapper

/**
 * Utility per il matching intelligente tra placeholder e prodotti reali.
 *
 * Responsabilità:
 * - Fuzzy matching su nomi di prodotti
 * - Normalizzazione stringhe per confronto affidabile
 * - Similarity scoring
 */
object PlaceholderMatcher {
    /**
     * Esegue un fuzzy match tra una query (placeholder) e un target (item reale).
     *
     * Logica:
     * - Normalizza entrambe le stringhe (lowercase, trim, special chars)
     * - Verifica se uno contiene l'altro (match esatto normalizzato)
     * - Calcola Levenshtein distance per match parziale
     * - Ritorna true se distance <= 2 (tolleranza typo minima)
     *
     * @param query nome placeholder (es. "Latte")
     * @param target nome item reale (es. "Latte intero 1L")
     * @return true se match, false altrimenti
     */
    fun fuzzyMatch(
        query: String,
        target: String,
    ): Boolean {
        val normalizedQuery = normalizeProductName(query)
        val normalizedTarget = normalizeProductName(target)

        // Exact match after normalization
        if (normalizedQuery == normalizedTarget || normalizedTarget.contains(normalizedQuery)) {
            return true
        }

        // Fuzzy match: if similarity is high enough (Levenshtein distance <= 2)
        val distance = levenshteinDistance(normalizedQuery, normalizedTarget)
        return distance <= 2 && normalizedQuery.length >= 3
    }

    /**
     * Normalizza il nome di un prodotto per il confronto.
     *
     * Trasformazioni:
     * - Lowercase
     * - Trim whitespace
     * - Rimuove accenti e caratteri speciali
     * - Spazi multipli → singolo spazio
     *
     * @param name nome prodotto raw
     * @return nome normalizzato per matching
     */
    fun normalizeProductName(name: String): String =
        name
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")

    /**
     * Calcola la distanza di Levenshtein tra due stringhe.
     *
     * Misura il numero minimo di edit (insert, delete, replace) per trasformare
     * una stringa in un'altra. Utile per tollerare typo e variazioni minori.
     *
     * @param a prima stringa
     * @param b seconda stringa
     * @return distanza Levenshtein (numero di operazioni)
     */
    private fun levenshteinDistance(
        a: String,
        b: String,
    ): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) {
            dp[i][0] = i
        }
        for (j in 0..b.length) {
            dp[0][j] = j
        }

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] =
                    minOf(
                        dp[i - 1][j] + 1, // delete
                        dp[i][j - 1] + 1, // insert
                        dp[i - 1][j - 1] + cost, // replace
                    )
            }
        }

        return dp[a.length][b.length]
    }

    /**
     * Calcola score di similarità tra due nomi (0.0 - 1.0).
     *
     * Usato per ranking di match multipli:
     * - 1.0 = match esatto
     * - 0.5-0.99 = match buono
     * - < 0.5 = match debole
     *
     * @param query placeholder name
     * @param target item name
     * @return score 0.0-1.0
     */
    fun similarityScore(
        query: String,
        target: String,
    ): Double {
        val normalizedQuery = normalizeProductName(query)
        val normalizedTarget = normalizeProductName(target)

        if (normalizedQuery == normalizedTarget) return 1.0
        if (normalizedTarget.contains(normalizedQuery)) return 0.95

        val distance = levenshteinDistance(normalizedQuery, normalizedTarget)
        val maxLength = maxOf(normalizedQuery.length, normalizedTarget.length)

        return if (maxLength == 0) 1.0 else 1.0 - (distance.toDouble() / maxLength)
    }
}

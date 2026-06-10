package com.mattiamularoni.saveeat.core.util

import java.time.Instant

/**
 * Conversioni condivise tra timestamp ISO8601 (remote) e epoch millis (locale).
 */
object DateTimeUtils {

    /**
     * Converte un timestamp ISO8601 in epoch millis.
     *
     * @param isoString timestamp ISO8601, oppure null
     * @param fallback valore restituito se [isoString] è null o non parsabile
     */
    fun parseIso8601OrDefault(isoString: String?, fallback: Long = System.currentTimeMillis()): Long =
        isoString?.let {
            try {
                Instant.parse(it).toEpochMilli()
            } catch (e: Exception) {
                fallback
            }
        } ?: fallback

    /**
     * Converte un timestamp ISO8601 in epoch millis, oppure null se [isoString]
     * è null o non parsabile.
     */
    fun parseIso8601OrNull(isoString: String?): Long? =
        isoString?.let {
            try {
                Instant.parse(it).toEpochMilli()
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Converte un timestamp epoch millis in stringa ISO8601.
     */
    fun formatToIso8601(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).toString()
}

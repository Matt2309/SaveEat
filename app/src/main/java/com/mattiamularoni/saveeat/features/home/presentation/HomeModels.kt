package com.mattiamularoni.saveeat.features.home.presentation

import com.mattiamularoni.saveeat.features.home.domain.repository.ExpiringItem
import com.mattiamularoni.saveeat.features.home.domain.repository.LeaderboardUser
import com.mattiamularoni.saveeat.features.home.domain.repository.SuggestedRecipe
import com.mattiamularoni.saveeat.features.home.domain.repository.UserProfile
import com.mattiamularoni.saveeat.features.home.domain.repository.UserStats

/**
 * UI-specific models per la home dashboard.
 *
 * Questi modelli sono trasformazioni dei domain models ottimizzate per la UI.
 * Includono dati calcolati (es. formattazione date, colori, badge) che sono
 * specifici della presentazione e non appartengono al domain layer.
 */

// Reexport domain models per semplicità (no UI-specific transformations needed yet)
typealias ExpiringItemUi = ExpiringItem
typealias LeaderboardUserUi = LeaderboardUser
typealias SuggestedRecipeUi = SuggestedRecipe
typealias UserStatsUi = UserStats
typealias UserProfileUi = UserProfile

/**
 * Enum per il freshness/urgency level di un item in scadenza.
 *
 * Usato per colorare/stilizzare gli item nella lista expiring items.
 * - EXPIRED: data scadenza nel passato
 * - CRITICAL: scade nei prossimi 1-2 giorni
 * - WARNING: scade entro 3-5 giorni
 * - NORMAL: scade entro 6-7 giorni
 */
enum class FreshnessLevel {
    EXPIRED,
    CRITICAL,
    WARNING,
    NORMAL;

    companion object {
        /**
         * Calcola il FreshnessLevel basandosi sul timestamp expiration.
         *
         * @param expirationTimeMs timestamp di scadenza in millisecondi
         * @return FreshnessLevel appropriato
         */
        fun fromExpirationTime(expirationTimeMs: Long): FreshnessLevel {
            val now = System.currentTimeMillis()
            val diffDays = (expirationTimeMs - now) / (1000 * 60 * 60 * 24)

            return when {
                diffDays < 0 -> EXPIRED
                diffDays <= 2 -> CRITICAL
                diffDays <= 5 -> WARNING
                else -> NORMAL
            }
        }
    }
}

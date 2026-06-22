package com.mattiamularoni.saveeat.features.recipes.domain.usecase

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository

/**
 * UseCase orchestratore per l'azione "Segna come cucinata".
 *
 * Responsabilità:
 * - Dedurre dalla dispensa gli ingredienti della ricetta (per nome)
 * - Calcolare gli eco-punti assegnati per la ricetta cucinata (dinamici sul peso stimato)
 * - Accumulare kg/euro/eco-punti nelle statistiche utente (unica fonte di verità: user_stats)
 *
 * Orchestratore tra RecipeRepository (implicito, la Recipe è già caricata) e
 * PantryRepository/StatsRepository, per non accoppiare direttamente i repository tra loro.
 */
class CookRecipeUseCase(
    private val pantryRepository: PantryRepository,
    private val statsRepository: StatsRepository,
    private val sessionProvider: SessionProvider
) {

    /**
     * Esegue l'azione "cucinata" per una ricetta.
     *
     * @param recipe ricetta segnata come cucinata
     * @return Result.success con gli eco-punti assegnati, Result.failure in caso di errore
     */
    suspend fun execute(recipe: Recipe): Result<Int> = runCatching {
        recipe.ingredients.forEach { ingredient ->
            pantryRepository.deductIngredientQuantity(ingredient.name, ingredient.amount)
        }

        val pointsAwarded = pointsFor(recipe)

        // Non fatale: se l'aggiornamento delle statistiche fallisce non deve invalidare
        // la cucinata (ingredienti già dedotti).
        statsRepository.addRecipeCookedStats(
            kg = recipe.estimatedWeightKg,
            euros = recipe.estimatedCostEuros,
            points = pointsAwarded
        ).onFailure {
            android.util.Log.e("CookRecipeUseCase", "Failed to update user stats: ${it.message}", it)
        }

        pointsAwarded
    }

    companion object {
        /** Eco-punti base assegnati per ogni ricetta cucinata, indipendentemente dal peso. */
        const val BASE_POINTS = 20

        /** Eco-punti aggiuntivi per ogni kg di cibo stimato "salvato" dalla ricetta. */
        const val POINTS_PER_KG = 30

        /**
         * Calcola gli eco-punti assegnati per una ricetta cucinata: punti base + punti
         * proporzionali al peso stimato salvato (es. una ricetta da ~1kg assegna ~50 punti).
         */
        fun pointsFor(recipe: Recipe): Int =
            BASE_POINTS + Math.round(recipe.estimatedWeightKg * POINTS_PER_KG).toInt()
    }
}

package com.mattiamularoni.saveeat.features.recipes.domain.usecase

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardRepository
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository

/**
 * UseCase orchestratore per l'azione "Segna come cucinata".
 *
 * Responsabilità:
 * - Dedurre dalla dispensa gli ingredienti della ricetta (per nome)
 * - Assegnare eco-punti all'utente per aver cucinato la ricetta
 * - Accumulare i kg/euro stimati risparmiati dalla ricetta nelle statistiche utente
 *
 * Orchestratore tra RecipeRepository (implicito, la Recipe è già caricata) e
 * PantryRepository/LeaderboardRepository/StatsRepository, per non accoppiare direttamente
 * i repository tra loro.
 */
class CookRecipeUseCase(
    private val pantryRepository: PantryRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val statsRepository: StatsRepository,
    private val sessionProvider: SessionProvider
) {

    /**
     * Esegue l'azione "cucinata" per una ricetta.
     *
     * @param recipe ricetta segnata come cucinata
     * @return Result.success se l'operazione è completata, Result.failure in caso di errore
     */
    suspend fun execute(recipe: Recipe): Result<Unit> = runCatching {
        recipe.ingredients.forEach { ingredient ->
            pantryRepository.deductIngredientQuantity(ingredient.name, ingredient.amount)
        }

        val userId = sessionProvider.getCurrentUserId()
        if (userId.isNotBlank()) {
            leaderboardRepository.updateEcoPoints(userId, COOK_RECIPE_POINTS)
        }

        // Non fatale: se l'aggiornamento delle statistiche fallisce non deve invalidare
        // la cucinata (ingredienti già dedotti, punti già assegnati).
        statsRepository.addSavings(recipe.estimatedWeightKg, recipe.estimatedCostEuros)
            .onFailure {
                android.util.Log.e("CookRecipeUseCase", "Failed to update user stats: ${it.message}", it)
            }
    }

    companion object {
        const val COOK_RECIPE_POINTS = 50
    }
}

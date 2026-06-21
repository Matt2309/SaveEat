package com.mattiamularoni.saveeat.features.recipes.domain.usecase

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardRepository
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe

/**
 * UseCase orchestratore per l'azione "Segna come cucinata".
 *
 * Responsabilità:
 * - Dedurre dalla dispensa gli ingredienti della ricetta (per nome)
 * - Assegnare eco-punti all'utente per aver cucinato la ricetta
 *
 * Orchestratore tra RecipeRepository (implicito, la Recipe è già caricata) e
 * PantryRepository/LeaderboardRepository, per non accoppiare direttamente i due repository.
 */
class CookRecipeUseCase(
    private val pantryRepository: PantryRepository,
    private val leaderboardRepository: LeaderboardRepository,
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

        // TODO(gamification): segnare la ricetta come "cucinata" / incrementare contatore
        // ricette cucinate quando il backend esporrà questa statistica.
    }

    companion object {
        const val COOK_RECIPE_POINTS = 50
    }
}

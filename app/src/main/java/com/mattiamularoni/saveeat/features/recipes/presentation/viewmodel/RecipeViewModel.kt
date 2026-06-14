package com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.recipes.domain.model.RecipeFilters
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.recipes.domain.repository.RecipeRepository
import com.mattiamularoni.saveeat.features.recipes.domain.usecase.GenerateRecipesUseCase
import com.mattiamularoni.saveeat.features.recipes.presentation.state.FavoriteRecipeUiState
import com.mattiamularoni.saveeat.features.recipes.presentation.state.GenerateRecipeUiState
import com.mattiamularoni.saveeat.features.recipes.presentation.state.RecipeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel per il modulo Recipes.
 *
 * Responsabilità:
 * - Gestire lo stato UI per ricette, preferiti, generazione
 * - Orchestrare le operazioni da RecipeRepository
 * - Handlere errori e aggiornamenti real-time
 * - Operare su viewModelScope (Dispatchers.Main.immediate di default)
 */
class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val generateRecipesUseCase: GenerateRecipesUseCase
) : ViewModel() {

    // ===== RECIPES STATE =====

    private val _recipesUiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val recipesUiState: StateFlow<RecipeUiState> = _recipesUiState.asStateFlow()

    // ===== FAVORITE RECIPES STATE =====

    private val _favoriteRecipesUiState =
        MutableStateFlow<FavoriteRecipeUiState>(FavoriteRecipeUiState.Loading)
    val favoriteRecipesUiState: StateFlow<FavoriteRecipeUiState> =
        _favoriteRecipesUiState.asStateFlow()

    // ===== GENERATE RECIPES STATE =====

    private val _generateRecipeUiState =
        MutableStateFlow<GenerateRecipeUiState>(GenerateRecipeUiState.Idle)
    val generateRecipeUiState: StateFlow<GenerateRecipeUiState> =
        _generateRecipeUiState.asStateFlow()

    // ===== OBSERVATION JOBS =====

    private var observeRecipesJob: Job? = null
    private var observeFavoriteRecipesJob: Job? = null

    // ===== INITIALIZATION =====

    init {
        observeRecipes()
    }

    // ===== RECIPES OPERATIONS =====

    /**
     * Osserva tutte le ricette disponibili con aggiornamenti real-time.
     *
     * Logica:
     * - Collect dal repository Flow
     * - Aggiorna _recipesUiState con Success
     * - Gestisce errori e lista vuota
     */
    private fun observeRecipes() {
        observeRecipesJob?.cancel()
        observeRecipesJob = viewModelScope.launch {
            recipeRepository
                .observeRecipes()
                .onStart {
                    _recipesUiState.value = RecipeUiState.Loading
                }
                .catch { exception ->
                    _recipesUiState.value = RecipeUiState.Error(
                        message = "Errore nel caricamento ricette",
                        exception = exception as? Exception
                    )
                }
                .collect { recipes ->
                    _recipesUiState.value = if (recipes.isEmpty()) {
                        RecipeUiState.Empty
                    } else {
                        RecipeUiState.Success(recipes = recipes)
                    }
                }
        }
    }

    /**
     * Sincronizza ricette dal backend.
     *
     * Operazione esplicita per pull-to-refresh o aggiornamento forzato.
     */
    fun syncRecipes() {
        viewModelScope.launch {
            try {
                val currentState = _recipesUiState.value
                if (currentState is RecipeUiState.Success) {
                    _recipesUiState.value = currentState.copy(isRefreshing = true)
                }

                recipeRepository.syncRecipes()
            } catch (e: Exception) {
                _recipesUiState.value = RecipeUiState.Error(
                    message = "Errore nella sincronizzazione",
                    exception = e
                )
            }
        }
    }

    /**
     * Cerca ricette per query di testo.
     *
     * @param query stringa di ricerca
     */
    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            observeRecipes()
            return
        }

        viewModelScope.launch {
            try {
                _recipesUiState.value = RecipeUiState.Loading
                val results = recipeRepository.searchRecipes(query)
                _recipesUiState.value = if (results.isEmpty()) {
                    RecipeUiState.Empty
                } else {
                    RecipeUiState.Success(recipes = results)
                }
            } catch (e: Exception) {
                _recipesUiState.value = RecipeUiState.Error(
                    message = "Errore nella ricerca",
                    exception = e
                )
            }
        }
    }

    /**
     * Filtra ricette per tag.
     *
     * @param tags lista di tag da filtrare
     */
    fun filterByTags(tags: List<String>) {
        viewModelScope.launch {
            try {
                _recipesUiState.value = RecipeUiState.Loading
                val results = recipeRepository.getRecipesByTags(tags)
                _recipesUiState.value = if (results.isEmpty()) {
                    RecipeUiState.Empty
                } else {
                    RecipeUiState.Success(recipes = results)
                }
            } catch (e: Exception) {
                _recipesUiState.value = RecipeUiState.Error(
                    message = "Errore nel filtro per tag",
                    exception = e
                )
            }
        }
    }

    /**
     * Recupera una singola ricetta per ID.
     *
     * @param recipeId UUID della ricetta
     */
    fun getRecipeDetail(recipeId: String): Recipe? {
        return (_recipesUiState.value as? RecipeUiState.Success)
            ?.recipes
            ?.firstOrNull { it.id == recipeId }
    }

    // ===== FAVORITE RECIPES OPERATIONS =====

    /**
     * Osserva ricette preferite dell'utente con aggiornamenti real-time.
     *
     * @param userId UUID dell'utente
     */
    fun observeFavoriteRecipes(userId: String) {
        observeFavoriteRecipesJob?.cancel()
        observeFavoriteRecipesJob = viewModelScope.launch {
            recipeRepository
                .observeFavoriteRecipes(userId)
                .onStart {
                    _favoriteRecipesUiState.value = FavoriteRecipeUiState.Loading
                }
                .catch { exception ->
                    _favoriteRecipesUiState.value = FavoriteRecipeUiState.Error(
                        message = "Errore nel caricamento preferiti",
                        exception = exception as? Exception
                    )
                }
                .collect { recipes ->
                    _favoriteRecipesUiState.value = if (recipes.isEmpty()) {
                        FavoriteRecipeUiState.Empty
                    } else {
                        FavoriteRecipeUiState.Success(recipes = recipes)
                    }
                }
        }
    }

    /**
     * Aggiunge una ricetta ai preferiti.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     */
    fun addFavoriteRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.addFavoriteRecipe(userId, recipeId)
            } catch (e: Exception) {
                _favoriteRecipesUiState.value = FavoriteRecipeUiState.Error(
                    message = "Errore nell'aggiungere ai preferiti",
                    exception = e
                )
            }
        }
    }

    /**
     * Rimuove una ricetta dai preferiti.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     */
    fun removeFavoriteRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.removeFavoriteRecipe(userId, recipeId)
            } catch (e: Exception) {
                _favoriteRecipesUiState.value = FavoriteRecipeUiState.Error(
                    message = "Errore nella rimozione dai preferiti",
                    exception = e
                )
            }
        }
    }

    // ===== GENERATE RECIPES OPERATIONS =====

    /**
     * Genera ricette intelligenti da ingredienti della dispensa.
     *
     * @param ingredients lista di ingredienti disponibili
     * @param preferences mappa di preferenze culinarie
     */
    fun generateRecipes(
        ingredients: List<String>,
        preferences: Map<String, Any> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                _generateRecipeUiState.value = GenerateRecipeUiState.Generating()
                val recipes = recipeRepository.generateRecipes(ingredients, preferences)
                _generateRecipeUiState.value = if (recipes.isEmpty()) {
                    GenerateRecipeUiState.Error("Nessuna ricetta generata")
                } else {
                    GenerateRecipeUiState.Success(recipes = recipes)
                }
            } catch (e: Exception) {
                _generateRecipeUiState.value = GenerateRecipeUiState.Error(
                    message = "Errore nella generazione ricette",
                    exception = e
                )
            }
        }
    }

    /**
     * Suggerisce ricette per ingredienti in scadenza.
     *
     * @param expiringItems lista di item in scadenza
     * @param preferences mappa di preferenze
     */
    fun suggestRecipesForExpiringItems(
        expiringItems: List<String>,
        preferences: Map<String, Any> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                _generateRecipeUiState.value = GenerateRecipeUiState.Generating()
                val recipes = recipeRepository.getSuggestedRecipesForExpiringItems(
                    expiringItems,
                    preferences
                )
                _generateRecipeUiState.value = if (recipes.isEmpty()) {
                    GenerateRecipeUiState.Error("Nessuna ricetta suggerita")
                } else {
                    GenerateRecipeUiState.Success(recipes = recipes)
                }
            } catch (e: Exception) {
                _generateRecipeUiState.value = GenerateRecipeUiState.Error(
                    message = "Errore nel suggerimento ricette",
                    exception = e
                )
            }
        }
    }

    /**
     * Recupera ricette stagionali.
     *
     * @param season stagione corrente
     * @param preferences mappa di preferenze
     */
    fun getSeasonalRecipes(
        season: String,
        preferences: Map<String, Any> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                _generateRecipeUiState.value = GenerateRecipeUiState.Generating()
                val recipes = recipeRepository.getSeasonalRecipes(season, preferences)
                _generateRecipeUiState.value = if (recipes.isEmpty()) {
                    GenerateRecipeUiState.Error("Nessuna ricetta stagionale disponibile")
                } else {
                    GenerateRecipeUiState.Success(recipes = recipes)
                }
            } catch (e: Exception) {
                _generateRecipeUiState.value = GenerateRecipeUiState.Error(
                    message = "Errore nel caricamento ricette stagionali",
                    exception = e
                )
            }
        }
    }

    /**
     * Resetta lo stato di generazione ricette.
     */
    fun resetGenerateState() {
        _generateRecipeUiState.value = GenerateRecipeUiState.Idle
    }

    /**
     * Genera ricette dagli ingredienti in scadenza della dispensa con filtri opzionali.
     *
     * @param filters filtri opzionali (stile cucina, tempo)
     */
    fun generateFromPantry(filters: RecipeFilters = RecipeFilters()) {
        viewModelScope.launch {
            _generateRecipeUiState.value = GenerateRecipeUiState.Generating()
            val result = generateRecipesUseCase(filters)
            _generateRecipeUiState.value = result.fold(
                onSuccess = { recipes ->
                    if (recipes.isEmpty()) GenerateRecipeUiState.Error("Nessuna ricetta generata")
                    else GenerateRecipeUiState.Success(recipes)
                },
                onFailure = { e ->
                    GenerateRecipeUiState.Error(
                        message = e.message ?: "Errore nella generazione ricette",
                        exception = e as? Exception
                    )
                }
            )
        }
    }
}

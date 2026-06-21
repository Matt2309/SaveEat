package com.mattiamularoni.saveeat.features.recipes.presentation.di

import com.mattiamularoni.saveeat.features.recipes.data.remote.GeminiRecipeDataSource
import com.mattiamularoni.saveeat.features.recipes.data.remote.GeminiRecipeDataSourceImpl
import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeRemoteDataSource
import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.recipes.data.repository.RecipeRepositoryImpl
import com.mattiamularoni.saveeat.features.recipes.domain.repository.RecipeRepository
import com.mattiamularoni.saveeat.features.recipes.domain.usecase.CookRecipeUseCase
import com.mattiamularoni.saveeat.features.recipes.domain.usecase.GenerateRecipesUseCase
import com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recipeScreenModule = module {
    single<GeminiRecipeDataSource> { GeminiRecipeDataSourceImpl() }

    factory<RecipeRemoteDataSource> {
        RecipeRemoteDataSourceImpl(
            supabaseClient = get(),
            geminiRecipeDataSource = get()
        )
    }

    factory<RecipeRepository> {
        RecipeRepositoryImpl(
            recipeDao = get(),
            remoteDataSource = get()
        )
    }

    factory { GenerateRecipesUseCase(pantryRepository = get(), recipeRepository = get()) }

    factory {
        CookRecipeUseCase(
            pantryRepository = get(),
            leaderboardRepository = get(),
            sessionProvider = get()
        )
    }

    viewModelOf(::RecipeViewModel)
}

package com.mattiamularoni.saveeat.features.recipes.presentation.di

import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeRemoteDataSource
import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.recipes.data.repository.RecipeRepositoryImpl
import com.mattiamularoni.saveeat.features.recipes.domain.repository.RecipeRepository
import com.mattiamularoni.saveeat.features.recipes.presentation.viewmodel.RecipeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Modulo Koin per la dependency injection del modulo Recipes.
 *
 * Registrazione di:
 * - Remote DataSource
 * - Repository implementation
 * - ViewModel
 */
val recipeScreenModule = module {
    // Remote DataSource
    factory<RecipeRemoteDataSource> {
        RecipeRemoteDataSourceImpl(supabaseClient = get())
    }

    // Repository
    factory<RecipeRepository> {
        RecipeRepositoryImpl(
            recipeDao = get(),
            remoteDataSource = get()
        )
    }

    // ViewModel
    viewModelOf(::RecipeViewModel)
}

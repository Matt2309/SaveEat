package com.mattiamularoni.saveeat.features.pantry.presentation.di

import com.mattiamularoni.saveeat.features.pantry.data.repository.PantryRepositoryImpl
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.domain.GetPantryItemsUseCase
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pantryScreenModule = module {
    factory<PantryRepository> {
        PantryRepositoryImpl(
            pantryDao = get(),
            supabaseClient = get()
        )
    }
    factory { GetPantryItemsUseCase(get()) }
    viewModelOf(::PantryViewModel)
}

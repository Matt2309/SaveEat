package com.mattiamularoni.saveeat.features.pantry.presentation.di

import com.mattiamularoni.saveeat.features.pantry.data.repository.PantryRepositoryImpl
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pantryModule = module {
    factory<PantryRepository> {
        PantryRepositoryImpl(
            pantryDao = get(),
            supabaseClient = get()
        )
    }
    viewModelOf(::PantryViewModel)
    // TODO: Add scoped qualifiers if multiple PantryRepository implementations are introduced.
}

package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.features.pantry.presentation.di.pantryModule
import org.koin.core.module.Module

val featureModules: List<Module> = listOf(
    pantryModule
)

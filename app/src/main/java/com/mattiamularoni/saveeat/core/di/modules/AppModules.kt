package com.mattiamularoni.saveeat.core.di.modules

import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    databaseModule,
    networkModule,
    uiModule,
    workerModule
)

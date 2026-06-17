package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.ui.theme.ThemeController
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val uiModule = module {
    single { ThemeController(androidApplication()) }
}

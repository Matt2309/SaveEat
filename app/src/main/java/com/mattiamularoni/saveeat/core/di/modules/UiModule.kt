package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.core.data.local.ProfilePhotoController
import com.mattiamularoni.saveeat.ui.theme.ThemeController
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val uiModule = module {
    single { ThemeController(androidApplication()) }
    single { ProfilePhotoController(androidApplication()) }
}

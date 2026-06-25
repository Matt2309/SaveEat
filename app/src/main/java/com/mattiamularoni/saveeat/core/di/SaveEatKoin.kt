package com.mattiamularoni.saveeat.core.di

import android.app.Application
import com.mattiamularoni.saveeat.core.di.modules.appModules
import com.mattiamularoni.saveeat.core.di.modules.featureModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

object SaveEatKoin {
    fun start(application: Application) {
        if (GlobalContext.getOrNull() != null) return
        startKoin {
            androidContext(application)
            modules(appModules + featureModules)
        }
    }
}

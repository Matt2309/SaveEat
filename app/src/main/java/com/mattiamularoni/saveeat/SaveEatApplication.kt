package com.mattiamularoni.saveeat

import android.app.Application
import com.mattiamularoni.saveeat.core.di.SaveEatKoin

class SaveEatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SaveEatKoin.start(this)
    }
}

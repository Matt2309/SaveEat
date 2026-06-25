package com.mattiamularoni.saveeat

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mattiamularoni.saveeat.core.di.SaveEatKoin
import com.mattiamularoni.saveeat.features.notifications.data.worker.NotificationWorker
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import java.util.concurrent.TimeUnit

class SaveEatApplication :
    Application(),
    Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()

    override fun onCreate() {
        super.onCreate()
        SaveEatKoin.start(this)
        scheduleNotificationWork()
    }

    private fun scheduleNotificationWork() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

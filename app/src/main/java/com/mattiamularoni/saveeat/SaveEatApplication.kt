package com.mattiamularoni.saveeat

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mattiamularoni.saveeat.common.worker.NotificationWorker
import com.mattiamularoni.saveeat.core.di.SaveEatKoin
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import java.util.concurrent.TimeUnit

class SaveEatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SaveEatKoin.start(this)
        initWorkManager()
        scheduleNotificationWork()
    }

    private fun initWorkManager() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()
        )
    }

    private fun scheduleNotificationWork() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

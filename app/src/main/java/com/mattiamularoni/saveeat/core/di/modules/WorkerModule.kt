package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.features.notifications.data.worker.NotificationWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val workerModule =
    module {
        workerOf(::NotificationWorker)
    }

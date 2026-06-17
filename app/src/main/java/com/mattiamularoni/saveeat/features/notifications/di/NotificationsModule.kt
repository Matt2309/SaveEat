package com.mattiamularoni.saveeat.features.notifications.di

import com.mattiamularoni.saveeat.features.notifications.domain.usecase.GetItemsDueForNotificationUseCase
import com.mattiamularoni.saveeat.features.notifications.domain.usecase.MarkItemsNotifiedUseCase
import org.koin.dsl.module

val notificationsModule = module {
    single { GetItemsDueForNotificationUseCase(get()) }
    single { MarkItemsNotifiedUseCase(get()) }
}

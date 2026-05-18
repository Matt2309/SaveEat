package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.core.data.remote.SupabaseClientProvider
import org.koin.dsl.module

val networkModule = module {
    single { SupabaseClientProvider.getOrCreate() }
}

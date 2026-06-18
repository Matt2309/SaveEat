package com.mattiamularoni.saveeat.core.di.modules

import androidx.room.Room
import com.mattiamularoni.saveeat.core.data.local.AppDatabase
import com.mattiamularoni.saveeat.core.data.local.migration.MIGRATION_4_5
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "saveeat.db"
        )
            .addMigrations(MIGRATION_4_5)
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }
    single { get<AppDatabase>().pantryDao() }
    single { get<AppDatabase>().recipeDao() }
    single { get<AppDatabase>().homeDao() }
}

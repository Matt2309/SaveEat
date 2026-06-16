package com.mattiamularoni.saveeat.core.di.modules

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mattiamularoni.saveeat.core.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pantry_items ADD COLUMN category_key TEXT NOT NULL DEFAULT ''")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pantry_item_assets (
                category_key TEXT NOT NULL PRIMARY KEY,
                names TEXT NOT NULL,
                image_url TEXT
            )
            """.trimIndent()
        )
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "saveeat.db"
        ).fallbackToDestructiveMigration(dropAllTables = false).build()
    }
    single { get<AppDatabase>().pantryDao() }
    single { get<AppDatabase>().pantryAssetDao() }
    single { get<AppDatabase>().recipeDao() }
    single { get<AppDatabase>().homeDao() }
}

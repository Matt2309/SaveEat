package com.mattiamularoni.saveeat.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE recipes ADD COLUMN estimatedWeightKg REAL NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE recipes ADD COLUMN estimatedCostEuros REAL NOT NULL DEFAULT 0")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_stats (
                    user_id TEXT NOT NULL PRIMARY KEY,
                    total_kg_saved REAL NOT NULL DEFAULT 0,
                    total_euros_saved REAL NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
        }
    }

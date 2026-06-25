package com.mattiamularoni.saveeat.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE user_stats ADD COLUMN total_eco_points INTEGER NOT NULL DEFAULT 0")
        }
    }

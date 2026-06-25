package com.mattiamularoni.saveeat.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE pantry_items ADD COLUMN category_key TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE pantry_items ADD COLUMN notified_at INTEGER")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pantry_item_assets (
                    category_key TEXT NOT NULL PRIMARY KEY,
                    names TEXT NOT NULL,
                    image_url TEXT
                )
                """.trimIndent(),
            )
        }
    }

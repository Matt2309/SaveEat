package com.mattiamularoni.saveeat.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shopping_list_items` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `normalizedName` TEXT NOT NULL,
                    `addedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_shopping_list_items_normalizedName` ON `shopping_list_items` (`normalizedName`)",
            )
        }
    }

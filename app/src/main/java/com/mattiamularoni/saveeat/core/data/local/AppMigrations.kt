package com.mattiamularoni.saveeat.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pantry_items ADD COLUMN image_url TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT DEFAULT NULL")
    }
}

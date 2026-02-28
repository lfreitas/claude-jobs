package com.claudejobs.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, TaskRunEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskRunDao(): TaskRunDao

    companion object {
        /**
         * Adds the five capability columns introduced in v2.
         * Existing tasks get sensible defaults: no tools, 4096 max tokens, empty system prompt.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN systemPrompt TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tasks ADD COLUMN maxTokens INTEGER NOT NULL DEFAULT 4096")
                db.execSQL("ALTER TABLE tasks ADD COLUMN enableWebSearch INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN enableWebFetch INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN enableCodeExecution INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

package dev.varshakulkarni.videoplayer.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    const val DB_VERSION = 2

    val MIGRATIONS: Array<Migration>
        get() = arrayOf<Migration>(
            migration12()
        )

    private fun migration12(): Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //TODO: add migration
        }
    }
}

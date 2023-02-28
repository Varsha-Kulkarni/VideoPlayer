package dev.varshakulkarni.videoplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.varshakulkarni.videoplayer.data.db.dao.VideosDao
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity


@Database(
    entities = [VideoEntity::class],
    version = DatabaseMigrations.DB_VERSION
)
abstract class VideoDatabase : RoomDatabase() {

    abstract fun getVideosDao(): VideosDao

    companion object {
        private const val DB_NAME = "YLVPlayer.db"

        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getInstance(context: Context): VideoDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    DB_NAME
                ).addMigrations(*DatabaseMigrations.MIGRATIONS).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}


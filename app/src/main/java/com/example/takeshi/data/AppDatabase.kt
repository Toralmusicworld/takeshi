package com.example.takeshi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HiddenMusic::class, Playlist::class, PlaylistItem::class, EqualizerSettings::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hiddenMusicDao(): HiddenMusicDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun equalizerDao(): EqualizerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "takeshi_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

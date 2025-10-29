package com.example.takeshi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EqualizerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: EqualizerSettings)

    @Query("SELECT * FROM equalizer_settings WHERE musicId = :musicId")
    suspend fun getSettingsForMusic(musicId: Long): EqualizerSettings?
}

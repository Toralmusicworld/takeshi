package com.example.takeshi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenMusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideMusic(hiddenMusic: HiddenMusic)

    @Query("DELETE FROM hidden_music WHERE musicId = :musicId")
    suspend fun unhideMusic(musicId: Long)

    @Query("SELECT * FROM hidden_music")
    fun getAllHiddenMusic(): Flow<List<HiddenMusic>>

    @Query("SELECT EXISTS(SELECT * FROM hidden_music WHERE musicId = :musicId)")
    suspend fun isHidden(musicId: Long): Boolean
}

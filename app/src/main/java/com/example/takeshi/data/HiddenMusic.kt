package com.example.takeshi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_music")
data class HiddenMusic(
    @PrimaryKey
    val musicId: Long
)

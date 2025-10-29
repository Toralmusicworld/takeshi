package com.example.takeshi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equalizer_settings")
data class EqualizerSettings(
    @PrimaryKey
    val musicId: Long,
    val band1Level: Int, // Levels will be stored in millibels (mB)
    val band2Level: Int,
    val band3Level: Int,
    val band4Level: Int,
    val band5Level: Int
)

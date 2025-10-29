package com.example.takeshi.data

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithMusic(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val items: List<PlaylistItem>
)

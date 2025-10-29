package com.example.takeshi.playback

import androidx.media3.common.Player
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper

class MusicNotificationProvider : MediaNotification.Provider {

    override fun createNotification(
        mediaSession: MediaSession,
        customCommands: MediaNotification.CustomCommands,
        actions: List<MediaNotification.Action>,
        onNotificationPostedCallback: MediaNotification.OnNotificationPostedCallback
    ): MediaNotification {
        val style = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                customCommands.get("STOP_ACTION")?.let { it.extras?.getParcelable("pending_intent") }
            )

        val notification = MediaNotification(
            1, // notification id
            mediaSession.player.playbackState,
            mediaSession.player.playWhenReady,
            style,
            onNotificationPostedCallback
        )
        return notification
    }

    override fun handleCustomCommand(
        mediaSession: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        if (action == "STOP_ACTION") {
            mediaSession.player.stop()
            mediaSession.player.clearMediaItems()
        }
        return true
    }
}

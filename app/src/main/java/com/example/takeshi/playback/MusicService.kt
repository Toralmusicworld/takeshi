package com.example.takeshi.playback

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.example.takeshi.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.media.audiofx.Equalizer

class MusicService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val equalizerDao by lazy { database.equalizerDao() }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaLibrarySession.Builder(this, player, object : MediaLibrarySession.Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.requestMetadata.mediaUri).build() }.toMutableList()
                return Futures.immediateFuture(updatedMediaItems)
            }
        }).build()

        setMediaNotificationProvider(MusicNotificationProvider())

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem?.mediaId?.toLongOrNull()?.let { musicId ->
                    applyEqualizerSettings(musicId)
                }
            }
        })
    }

    private fun applyEqualizerSettings(musicId: Long) {
        serviceScope.launch {
            val settings = equalizerDao.getSettingsForMusic(musicId)
            try {
                val equalizer = Equalizer(0, player.audioSessionId)
                equalizer.enabled = true
                if (settings != null) {
                    // Assuming a 5-band equalizer, we apply the settings.
                    // The band indices are 0-based.
                    equalizer.setBandLevel(0, settings.band1Level.toShort())
                    equalizer.setBandLevel(1, settings.band2Level.toShort())
                    equalizer.setBandLevel(2, settings.band3Level.toShort())
                    equalizer.setBandLevel(3, settings.band4Level.toShort())
                    equalizer.setBandLevel(4, settings.band5Level.toShort())
                } else {
                    // Reset to flat if no settings are saved
                    for (i in 0 until equalizer.numberOfBands) {
                        equalizer.setBandLevel(i.toShort(), 0)
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., device doesn't support equalizer
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}

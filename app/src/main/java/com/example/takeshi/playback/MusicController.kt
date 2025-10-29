package com.example.takeshi.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

object MusicController {

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    val mediaController: MediaController?
        get() = if (mediaControllerFuture?.isDone == true) mediaControllerFuture?.get() else null

    fun initialize(context: Context) {
        if (mediaControllerFuture == null) {
            val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
            mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            mediaControllerFuture?.addListener({}, MoreExecutors.directExecutor())
        }
    }

    fun release() {
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaControllerFuture = null
    }
}

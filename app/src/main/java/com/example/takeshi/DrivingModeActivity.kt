package com.example.takeshi

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.takeshi.databinding.ActivityDrivingModeBinding
import com.example.takeshi.playback.MusicController
import kotlin.math.abs
import androidx.media3.common.Player

class DrivingModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrivingModeBinding
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrivingModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGestureDetector()
        setupPlayerControls()
        observePlayerState()
    }

    private fun setupPlayerControls() {
        binding.drivingPlayPauseButton.setOnClickListener { MusicController.mediaController?.playWhenReady = !MusicController.mediaController?.playWhenReady!! }
        binding.drivingNextButton.setOnClickListener { MusicController.mediaController?.seekToNext() }
        binding.drivingPrevButton.setOnClickListener { MusicController.mediaController?.seekToPrevious() }
    }

    private fun observePlayerState() {
        MusicController.mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateUi(mediaItem)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                binding.drivingPlayPauseButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        })
        updateUi(MusicController.mediaController?.currentMediaItem)
    }

    private fun updateUi(mediaItem: androidx.media3.common.MediaItem?) {
        if (mediaItem == null) return
        binding.drivingSongTitle.text = mediaItem.mediaMetadata.title
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - e1.x
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) { // Swipe left
                        onSwipeLeft()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun onSwipeLeft() {
        MusicListDialogFragment().show(supportFragmentManager, "MusicListDialog")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event != null) {
            gestureDetector.onTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
    }
}

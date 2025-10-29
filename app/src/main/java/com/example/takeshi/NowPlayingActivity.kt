package com.example.takeshi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.data.HiddenMusic
import com.example.takeshi.databinding.ActivityNowPlayingBinding
import com.example.takeshi.playback.MusicController
import kotlinx.coroutines.launch

class NowPlayingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNowPlayingBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val hiddenMusicDao by lazy { database.hiddenMusicDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNowPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPlayerControls()
        observePlayerState()
    }

    private fun setupToolbar() {
        binding.toolbarNowPlaying.setNavigationOnClickListener { finish() }
        binding.toolbarNowPlaying.inflateMenu(R.menu.now_playing_menu)
        binding.toolbarNowPlaying.setOnMenuItemClickListener { item ->
            val currentMediaId = MusicController.mediaController?.currentMediaItem?.mediaId?.toLongOrNull()
            if (currentMediaId == null) return@setOnMenuItemClickListener false

            when (item.itemId) {
                R.id.action_hide_now_playing -> {
                    hideCurrentMusic(currentMediaId)
                    true
                }
                R.id.action_equalizer_now_playing -> {
                    EqualizerDialogFragment.newInstance(currentMediaId)
                        .show(supportFragmentManager, "EqualizerDialog")
                    true
                }
                else -> false
            }
        }
    }

    private fun setupPlayerControls() {
        binding.playPauseButtonNowPlaying.setOnClickListener { MusicController.mediaController?.playWhenReady = !MusicController.mediaController?.playWhenReady!! }
        binding.nextButtonNowPlaying.setOnClickListener { MusicController.mediaController?.seekToNext() }
        binding.prevButtonNowPlaying.setOnClickListener { MusicController.mediaController?.seekToPrevious() }
    }

    private fun observePlayerState() {
        MusicController.mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateUi(mediaItem)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                binding.playPauseButtonNowPlaying.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        })
        updateUi(MusicController.mediaController?.currentMediaItem)
    }

    private fun updateUi(mediaItem: androidx.media3.common.MediaItem?) {
        if (mediaItem == null) return
        binding.songTitleNowPlaying.text = mediaItem.mediaMetadata.title
        binding.songArtistNowPlaying.text = mediaItem.mediaMetadata.artist
        // Load album art with Glide/Coil
    }

    private fun hideCurrentMusic(musicId: Long) {
        lifecycleScope.launch {
            hiddenMusicDao.hideMusic(HiddenMusic(musicId))
            // Also skip to the next song
            MusicController.mediaController?.seekToNext()
            android.widget.Toast.makeText(this@NowPlayingActivity, "Song hidden", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

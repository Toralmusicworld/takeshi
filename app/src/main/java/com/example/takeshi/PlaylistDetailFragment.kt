package com.example.takeshi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.databinding.FragmentPlaylistDetailBinding
import com.example.takeshi.playback.MusicController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val playlistDao by lazy { database.playlistDao() }
    private var playlistId: Long = -1

    companion object {
        private const val ARG_PLAYLIST_ID = "playlist_id"

        fun newInstance(playlistId: Long): PlaylistDetailFragment {
            val fragment = PlaylistDetailFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong(ARG_PLAYLIST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadPlaylistMusic()
        return binding.root
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(emptyList(),
            onMusicClicked = { music ->
                playMusic(music)
            },
            onMoreOptionsClicked = {
                // Options can be implemented similarly to HomeFragment
            }
        )
        binding.playlistDetailRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadPlaylistMusic() {
        lifecycleScope.launch {
            val allMusic = MusicLoader.scanDeviceForMusic(requireContext())
            val playlistWithMusic = playlistDao.getPlaylistWithMusic(playlistId).first()
            val musicInPlaylistIds = playlistWithMusic.items.map { it.musicId }.toSet()
            val musicInPlaylist = allMusic.filter { it.id in musicInPlaylistIds }
            musicAdapter.updateList(musicInPlaylist)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playMusic(startMusic: Music) {
        val mediaItems = musicAdapter.musicList.map { music ->
            MediaItem.Builder()
                .setMediaId(music.id.toString())
                .setUri(music.path)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(music.title)
                        .setArtist(music.artist)
                        .setAlbumTitle(music.album)
                        .build()
                )
                .build()
        }
        val startIndex = musicAdapter.musicList.indexOf(startMusic)
        MusicController.mediaController?.setMediaItems(mediaItems, startIndex, 0)
        MusicController.mediaController?.prepare()
        MusicController.mediaController?.play()
    }
}

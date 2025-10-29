package com.example.takeshi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takeshi.databinding.FragmentMusicListDialogBinding
import com.example.takeshi.playback.MusicController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MusicListDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMusicListDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicListDialogBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadMusic()
        return binding.root
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(emptyList(),
            onMusicClicked = { music ->
                MusicController.mediaController?.seekToDefaultPosition(musicAdapter.musicList.indexOf(music))
                dismiss()
            },
            onMoreOptionsClicked = {}
        )
        binding.musicListRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadMusic() {
        val mediaItems = MusicController.mediaController?.let { controller ->
            (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it) }
        } ?: emptyList()

        // This part is a bit tricky as we don't have the full Music object here.
        // For simplicity, we create a simplified Music object from MediaMetadata.
        val musicList = mediaItems.map {
            Music(
                id = it.mediaId.toLong(),
                title = it.mediaMetadata.title.toString(),
                artist = it.mediaMetadata.artist.toString(),
                album = "", duration = 0, path = "", albumArtUri = null
            )
        }
        musicAdapter.updateList(musicList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.takeshi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takeshi.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import android.widget.PopupMenu
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.data.HiddenMusic
import com.example.takeshi.playback.MusicController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import androidx.media3.common.MediaItem

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val hiddenMusicDao by lazy { database.hiddenMusicDao() }
    private val playlistDao by lazy { database.playlistDao() }
    private val equalizerDao by lazy { database.equalizerDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(emptyList(),
            onMusicClicked = { music ->
                playMusic(music)
            },
            onMoreOptionsClicked = { music ->
                showPopupMenu(music)
            }
        )
        binding.musicRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun loadMusic() {
        lifecycleScope.launch {
            val allMusic = MusicLoader.scanDeviceForMusic(requireContext())
            val hiddenMusicIds = hiddenMusicDao.getAllHiddenMusic().first().map { it.musicId }.toSet()
            val visibleMusic = allMusic.filter { it.id !in hiddenMusicIds }
            musicAdapter.updateList(visibleMusic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showPopupMenu(music: Music) {
        val view = binding.musicRecyclerView.findViewHolderForAdapterPosition(
            (binding.musicRecyclerView.adapter as MusicAdapter)
                .musicList.indexOf(music)
        )?.itemView?.findViewById<View>(R.id.more_options) ?: return

        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.list_item_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_hide -> {
                    hideMusic(music)
                    true
                }
                R.id.action_add_to_playlist -> {
                    showPlaylistsDialog(music)
                    true
                }
                R.id.action_equalizer -> {
                    EqualizerDialogFragment.newInstance(music.id)
                        .show(parentFragmentManager, "EqualizerDialog")
                    true
                }
                R.id.action_share -> {
                    shareMusic(music)
                    true
                }
                R.id.action_delete -> {
                    deleteMusic(music)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun hideMusic(music: Music) {
        lifecycleScope.launch {
            hiddenMusicDao.hideMusic(HiddenMusic(music.id))
            loadMusic() // Refresh the list
        }
    }

    private fun showPlaylistsDialog(music: Music) {
        lifecycleScope.launch {
            val playlists = playlistDao.getAllPlaylists().first()
            val playlistNames = playlists.map { it.name }.toTypedArray()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add to Playlist")
                .setItems(playlistNames) { dialog, which ->
                    val selectedPlaylist = playlists[which]
                    addMusicToPlaylist(music, selectedPlaylist)
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun addMusicToPlaylist(music: Music, playlist: com.example.takeshi.data.Playlist) {
        lifecycleScope.launch {
            playlistDao.addMusicToPlaylist(com.example.takeshi.data.PlaylistItem(playlist.id, music.id))
        }
    }

    private fun shareMusic(music: Music) {
        val file = java.io.File(music.path)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            type = "audio/*"
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Music"))
    }

    private fun deleteMusic(music: Music) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Music")
            .setMessage("Are you sure you want to delete '${music.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(music)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(music: Music) {
        try {
            val contentUri = android.content.ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                music.id
            )
            val deletedRows = requireContext().contentResolver.delete(contentUri, null, null)
            if (deletedRows > 0) {
                android.widget.Toast.makeText(requireContext(), "Music deleted", android.widget.Toast.LENGTH_SHORT).show()
                loadMusic()
            } else {
                android.widget.Toast.makeText(requireContext(), "Failed to delete music", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
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

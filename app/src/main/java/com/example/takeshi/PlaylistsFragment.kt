package com.example.takeshi

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.data.Playlist
import com.example.takeshi.databinding.FragmentPlaylistsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var playlistAdapter: PlaylistAdapter
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val playlistDao by lazy { database.playlistDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observePlaylists()

        binding.fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlaylistDetailFragment.newInstance(playlist.id))
                .addToBackStack(null)
                .commit()
        }
        binding.playlistsRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observePlaylists() {
        lifecycleScope.launch {
            playlistDao.getAllPlaylists().collectLatest { playlists ->
                playlistAdapter.updateList(playlists)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            hint = "Playlist Name"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Playlist")
            .setView(editText)
            .setPositiveButton("Create") { dialog, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    createPlaylist(name)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createPlaylist(name: String) {
        lifecycleScope.launch {
            playlistDao.createPlaylist(Playlist(name = name))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.takeshi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.databinding.FragmentHiddenFolderBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HiddenFolderFragment : Fragment() {

    private var _binding: FragmentHiddenFolderBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val hiddenMusicDao by lazy { database.hiddenMusicDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiddenFolderBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadHiddenMusic()
        return binding.root
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(emptyList(),
            onMusicClicked = {
                // Handle music click
            },
            onMoreOptionsClicked = { music ->
                showPopupMenu(music)
            }
        )
        binding.hiddenMusicRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadHiddenMusic() {
        lifecycleScope.launch {
            val allMusic = MusicLoader.scanDeviceForMusic(requireContext())
            val hiddenMusicIds = hiddenMusicDao.getAllHiddenMusic().first().map { it.musicId }.toSet()
            val hiddenMusic = allMusic.filter { it.id in hiddenMusicIds }
            musicAdapter.updateList(hiddenMusic)
        }
    }

    private fun showPopupMenu(music: Music) {
        val view = binding.hiddenMusicRecyclerView.findViewHolderForAdapterPosition(
            (binding.hiddenMusicRecyclerView.adapter as MusicAdapter)
                .musicList.indexOf(music)
        )?.itemView?.findViewById<View>(R.id.more_options) ?: return

        val popup = PopupMenu(context, view)
        // For simplicity, we reuse the same menu. In a real app, you might create a different menu for unhiding.
        popup.menuInflater.inflate(R.menu.list_item_menu, popup.menu)
        val hideMenuItem = popup.menu.findItem(R.id.action_hide)
        hideMenuItem.title = "Unhide"

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_hide -> {
                    unhideMusic(music)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun unhideMusic(music: Music) {
        lifecycleScope.launch {
            hiddenMusicDao.unhideMusic(music.id)
            loadHiddenMusic() // Refresh the list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

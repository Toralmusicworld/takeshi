package com.example.takeshi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.takeshi.data.Playlist
import com.example.takeshi.databinding.ListItemPlaylistBinding

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onPlaylistClicked: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ListItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updateList(newList: List<Playlist>) {
        playlists = newList
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(private val binding: ListItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            itemView.setOnClickListener { onPlaylistClicked(playlist) }
        }
    }
}

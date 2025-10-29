package com.example.takeshi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.takeshi.databinding.ListItemMusicBinding
import java.util.concurrent.TimeUnit

class MusicAdapter(
    var musicList: List<Music>,
    private val onMusicClicked: (Music) -> Unit,
    private val onMoreOptionsClicked: (Music) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ListItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.bind(music)
    }

    override fun getItemCount(): Int = musicList.size

    fun updateList(newList: List<Music>) {
        musicList = newList
        notifyDataSetChanged()
    }

    inner class MusicViewHolder(private val binding: ListItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(music: Music) {
            binding.musicTitle.text = music.title
            binding.musicArtist.text = music.artist
            binding.musicDuration.text = formatDuration(music.duration)
            // Here you would load album art with a library like Glide or Coil
            // binding.albumArt.load(music.albumArtUri)

            itemView.setOnClickListener { onMusicClicked(music) }
            binding.moreOptions.setOnClickListener { onMoreOptionsClicked(music) }
        }

        private fun formatDuration(duration: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
            return String.format("%d:%02d", minutes, seconds)
        }
    }
}

package com.example.videoreelsapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoreelsapp.databinding.ItemReelBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class ReelsAdapter(private val videoUrls: List<String>) :
    RecyclerView.Adapter<ReelsAdapter.ReelViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer?>()

    inner class ReelViewHolder(val binding: ItemReelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val binding = ItemReelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        val context = holder.itemView.context
        val player = ExoPlayer.Builder(context).build()
        holder.binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(videoUrls[position])
        player.setMediaItem(mediaItem)
        player.prepare()

        // Start playback after surface is ready
        holder.binding.playerView.post {
            player.playWhenReady = true
        }

        players[position] = player
    }


    override fun getItemCount() = videoUrls.size

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.adapterPosition
        players[position]?.release()
        players.remove(position)
    }

    fun releaseAllPlayers() {
        players.values.forEach { it?.release() }
        players.clear()
    }
}




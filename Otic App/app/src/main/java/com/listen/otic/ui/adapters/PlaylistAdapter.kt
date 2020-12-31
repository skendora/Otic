/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.listen.otic.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.listen.otic.R
import com.listen.otic.databinding.ItemPlaylistBinding
import com.listen.otic.models.Playlist
import com.listen.otic.extensions.inflateWithBinding

class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    var playlists: List<Playlist> = emptyList()
        private set

    fun updateData(playlists: List<Playlist>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateWithBinding(R.layout.item_playlist))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    class ViewHolder constructor(var binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlist = playlist
            binding.executePendingBindings()
        }
    }
}

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
package com.listen.otic.ui.fragments.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.listen.otic.R
import com.listen.otic.constants.Constants.ARTIST
import com.listen.otic.databinding.FragmentArtistDetailBinding
import com.listen.otic.extensions.addOnItemClick
import com.listen.otic.extensions.argument
import com.listen.otic.extensions.filter
import com.listen.otic.extensions.getExtraBundle
import com.listen.otic.extensions.inflateWithBinding
import com.listen.otic.extensions.observe
import com.listen.otic.extensions.safeActivity
import com.listen.otic.extensions.toSongIds
import com.listen.otic.models.Artist
import com.listen.otic.models.Song
import com.listen.otic.repository.AlbumRepository
import com.listen.otic.ui.adapters.AlbumAdapter
import com.listen.otic.ui.adapters.SongsAdapter
import com.listen.otic.ui.fragments.base.MediaItemFragment
import com.listen.otic.util.AutoClearedValue
import com.listen.otic.util.doAsyncPostWithResult
import kotlinx.android.synthetic.main.fragment_artist_detail.recyclerView
import kotlinx.android.synthetic.main.fragment_artist_detail.rvArtistAlbums
import org.koin.android.ext.android.inject

class ArtistDetailFragment : MediaItemFragment() {
    lateinit var artist: Artist
    var binding by AutoClearedValue<FragmentArtistDetailBinding>(this)

    private val albumRepository by inject<AlbumRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        artist = argument(ARTIST)
        binding = inflater.inflateWithBinding(R.layout.fragment_artist_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.artist = artist

        val adapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        recyclerView.layoutManager = LinearLayoutManager(safeActivity)
        recyclerView.adapter = adapter

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    adapter.updateData(list as List<Song>)
                }

        recyclerView.addOnItemClick { position: Int, _: View ->
            val extras = getExtraBundle(adapter.songs.toSongIds(), artist.name)
            mainViewModel.mediaItemClicked(adapter.songs[position], extras)
        }

        setupArtistAlbums()
    }

    private fun setupArtistAlbums() {
        val albumsAdapter = AlbumAdapter(true)
        rvArtistAlbums.apply {
            layoutManager = LinearLayoutManager(safeActivity, HORIZONTAL, false)
            adapter = albumsAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumsAdapter.albums[position], null)
            }
        }

        // TODO get rid of this by using a view model for loading /w coroutines
        doAsyncPostWithResult(handler = {
            albumRepository.getAlbumsForArtist(artist.id)
        }, postHandler = { albums ->
            albums?.let { albumsAdapter.updateData(it) }
        }).execute()
    }
}

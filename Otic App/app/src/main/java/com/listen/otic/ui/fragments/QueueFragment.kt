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
package com.listen.otic.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.listen.otic.R
import com.listen.otic.constants.Constants.ACTION_QUEUE_REORDER
import com.listen.otic.constants.Constants.QUEUE_FROM
import com.listen.otic.constants.Constants.QUEUE_TO
import com.listen.otic.extensions.addOnItemClick
import com.listen.otic.extensions.getExtraBundle
import com.listen.otic.extensions.inflateTo
import com.listen.otic.extensions.keepInOrder
import com.listen.otic.extensions.observe
import com.listen.otic.extensions.toSongIds
import com.listen.otic.models.QueueData
import com.listen.otic.repository.SongsRepository
import com.listen.otic.ui.adapters.SongsAdapter
import com.listen.otic.ui.fragments.base.BaseNowPlayingFragment
import com.listen.otic.ui.widgets.DragSortRecycler
import com.listen.otic.util.doAsyncPostWithResult
import kotlinx.android.synthetic.main.fragment_queue.recyclerView
import kotlinx.android.synthetic.main.fragment_queue.tvQueueTitle
import org.koin.android.ext.android.inject

class QueueFragment : BaseNowPlayingFragment() {
    lateinit var adapter: SongsAdapter
    private lateinit var queueData: QueueData
    private var isReorderFromUser = false

    private val songsRepository by inject<SongsRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.fragment_queue, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter().apply {
            isQueue = true
            popupMenuListener = mainViewModel.popupMenuListener
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@QueueFragment.adapter
        }

        nowPlayingViewModel.queueData.observe(this) { data ->
            this.queueData = data
            tvQueueTitle.text = data.queueTitle
            if (data.queue.isNotEmpty()) {
                fetchQueueSongs(data.queue)
            }
        }

        recyclerView.addOnItemClick { position, _ ->
            adapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(adapter.songs.toSongIds(), queueData.queueTitle)
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
    }

    private fun fetchQueueSongs(queue: LongArray) {
        //to avoid lag when reordering queue, we don't re-fetch queue if we know the reorder was from user
        if (isReorderFromUser) {
            isReorderFromUser = false
            return
        }

        // TODO use coroutines and prefer this sort of logic in a view model
        doAsyncPostWithResult(handler = {
            songsRepository.getSongsForIds(queue).keepInOrder(queue)
        }, postHandler = {
            if (it != null) {
                adapter.updateData(it)

                val dragSortRecycler = DragSortRecycler().apply {
                    setViewHandleId(R.id.ivReorder)
                    setOnItemMovedListener { from, to ->
                        isReorderFromUser = true
                        adapter.reorderSong(from, to)

                        val extras = Bundle().apply {
                            putInt(QUEUE_FROM, from)
                            putInt(QUEUE_TO, to)
                        }
                        mainViewModel.transportControls().sendCustomAction(ACTION_QUEUE_REORDER, extras)
                    }
                }

                recyclerView.run {
                    addItemDecoration(dragSortRecycler)
                    addOnItemTouchListener(dragSortRecycler)
                    addOnScrollListener(dragSortRecycler.scrollListener)
                }
            }
        }).execute()
    }
}

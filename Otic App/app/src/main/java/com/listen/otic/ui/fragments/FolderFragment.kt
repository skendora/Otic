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
import com.afollestad.rxkprefs.Pref
import com.listen.otic.PREF_LAST_FOLDER
import com.listen.otic.R
import com.listen.otic.extensions.getExtraBundle
import com.listen.otic.extensions.inflateTo
import com.listen.otic.extensions.safeActivity
import com.listen.otic.repository.FoldersRepository
import com.listen.otic.repository.SongsRepository
import com.listen.otic.ui.adapters.FolderAdapter
import com.listen.otic.ui.fragments.base.MediaItemFragment
import kotlinx.android.synthetic.main.layout_recyclerview_padding.recyclerView
import org.koin.android.ext.android.inject

class FolderFragment : MediaItemFragment() {
    private lateinit var folderAdapter: FolderAdapter

    private val songsRepository by inject<SongsRepository>()
    private val foldersRepository by inject<FoldersRepository>()
    private val lastFolderPref by inject<Pref<String>>(name = PREF_LAST_FOLDER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview_padding, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        folderAdapter = FolderAdapter(safeActivity, songsRepository, foldersRepository, lastFolderPref)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = folderAdapter
        }
        folderAdapter.init(callback = { song, queueIds, title ->
            val extras = getExtraBundle(queueIds, title)
            mainViewModel.mediaItemClicked(song, extras)
        })
    }
}

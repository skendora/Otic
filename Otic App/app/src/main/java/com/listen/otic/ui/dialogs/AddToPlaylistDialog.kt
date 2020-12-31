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
package com.listen.otic.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems
import com.listen.otic.R
import com.listen.otic.models.MediaID.Companion.CALLER_SELF
import com.listen.otic.models.Song
import com.listen.otic.repository.PlaylistRepository
import com.listen.otic.constants.Constants.SONGS
import com.listen.otic.util.MusicUtils
import org.koin.android.ext.android.inject

class AddToPlaylistDialog : DialogFragment(), CreatePlaylistDialog.PlaylistCreatedCallback {

    companion object {
        private const val TAG = "AddToPlaylistDialog"

        fun show(activity: FragmentActivity, song: Song? = null) {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            show(activity, songs)
        }

        fun show(activity: FragmentActivity, songList: LongArray) {
            val dialog = AddToPlaylistDialog().apply {
                arguments = Bundle().apply { putLongArray(SONGS, songList) }
            }
            dialog.show(activity.supportFragmentManager, TAG)
        }
    }

    var callback: () -> Unit? = {
        null
    }
    private val playlistRepository by inject<PlaylistRepository>()

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("Not attached")
        val playlists = playlistRepository.getPlaylists(CALLER_SELF)
        val itemList = mutableListOf<String>().apply {
            add(getString(R.string.create_new_playlist))
            addAll(playlists.map { it.name })
        }

        return MaterialDialog(context).show {
            title(R.string.add_to_playlist)
            listItems(items = itemList) { _, index, _ ->
                val songs = arguments?.getLongArray(SONGS) ?: return@listItems
                if (index == 0) {
                    CreatePlaylistDialog.show(this@AddToPlaylistDialog, songs)
                } else {
                    MusicUtils.addToPlaylist(context, songs, playlists[index - 1].id)
                }
            }
            onDismiss {
                // Make sure the DialogFragment dismisses as well
                this@AddToPlaylistDialog.dismiss()
            }
        }
    }

    override fun onPlaylistCreated() = dismiss()
}

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
package com.listen.otic.repository

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Playlists._ID
import com.listen.otic.extensions.mapList
import com.listen.otic.extensions.value
import com.listen.otic.models.MediaID
import com.listen.otic.models.Playlist
import com.listen.otic.models.Song
import com.listen.otic.util.Utils.MUSIC_ONLY_SELECTION

const val YIELD_FREQUENCY = 100

interface PlaylistRepository {

    fun getPlaylists(caller: String?): List<Playlist>

    fun getSongsInPlaylist(playlistID: Long, caller: String?): List<Song>

    fun deletePlaylists(playlistId: Long): Int
}

class RealPlaylistRepository(
    private val contentResolver: ContentResolver
) : PlaylistRepository {

    override fun getPlaylists(caller: String?): List<Playlist> {
        MediaID.currentCaller = caller
        return makePlaylistCursor().mapList(true) {
            val id: Long = value(_ID)
            val songCount = getSongCountForPlaylist(id)
            Playlist.fromCursor(this, songCount)
        }.filter { it.name.isNotEmpty() }
    }

    override fun getSongsInPlaylist(playlistID: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        val playlistCount = countPlaylist(playlistID)

        makePlaylistSongCursor(playlistID)?.use {
            var runCleanup = false
            if (it.count != playlistCount) {
                runCleanup = true
            }

            if (!runCleanup && it.moveToFirst()) {
                val playOrderCol = it.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)
                var lastPlayOrder = -1
                do {
                    val playOrder = it.getInt(playOrderCol)
                    if (playOrder == lastPlayOrder) {
                        runCleanup = true
                        break
                    }
                    lastPlayOrder = playOrder
                } while (it.moveToNext())
            }

            if (runCleanup) {
                cleanupPlaylist(playlistID, it, true)
            }
        }

        return makePlaylistSongCursor(playlistID)
                .mapList(true, Song.Companion::fromPlaylistMembersCursor)
    }

    override fun deletePlaylists(playlistId: Long): Int {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder().apply {
            append("_id IN (")
            append(playlistId)
            append(")")
        }
        return contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun makePlaylistCursor(): Cursor? {
        return contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME), null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER)
    }

    private fun getSongCountForPlaylist(playlistId: Long): Int {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        return contentResolver.query(uri, arrayOf(_ID), MUSIC_ONLY_SELECTION, null, null)?.use {
            if (it.moveToFirst()) {
                it.count
            } else {
                0
            }
        } ?: 0
    }

    private fun cleanupPlaylist(
        playlistId: Long,
        cursor: Cursor,
        closeCursorAfter: Boolean
    ) {
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        val ops = arrayListOf<ContentProviderOperation>().apply {
            add(ContentProviderOperation.newDelete(uri).build())
        }

        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                val builder = ContentProviderOperation.newInsert(uri)
                        .withValue(MediaStore.Audio.Playlists.Members.PLAY_ORDER, cursor.position)
                        .withValue(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(idCol))
                if ((cursor.position + 1) % YIELD_FREQUENCY == 0) {
                    builder.withYieldAllowed(true)
                }
                ops.add(builder.build())
            } while (cursor.moveToNext())
        }

        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, ops)
        } catch (e: RemoteException) {
        } catch (e: OperationApplicationException) {
        }

        if (closeCursorAfter) {
            cursor.close()
        }
    }

    private fun countPlaylist(playlistId: Long): Int {
        return contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID),
                null,
                null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        )?.use {
            if (it.moveToFirst()) {
                it.count
            } else {
                0
            }
        } ?: 0
    }

    private fun makePlaylistSongCursor(playlistID: Long?): Cursor? {
        val selection = StringBuilder().apply {
            append("${MediaStore.Audio.AudioColumns.IS_MUSIC}=1")
            append(" AND ${MediaStore.Audio.AudioColumns.TITLE} != ''")
        }
        val projection = arrayOf(
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.ARTIST_ID,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.TRACK,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER
        )
        return contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID!!),
                projection,
                selection.toString(),
                null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        )
    }
}

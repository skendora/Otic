
package com.listen.otic.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.FIRST_YEAR
import android.provider.MediaStore.Audio.Media.DEFAULT_SORT_ORDER
import android.provider.MediaStore.Audio.Media.TRACK
import com.afollestad.rxkprefs.Pref
import com.listen.otic.constants.AlbumSortOrder
import com.listen.otic.extensions.mapList
import com.listen.otic.models.Album
import com.listen.otic.models.MediaID
import com.listen.otic.models.Song
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI as ALBUMS_URI
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI as SONGS_URI

interface AlbumRepository {

    fun getAllAlbums(caller: String?): List<Album>

    fun getAlbum(id: Long): Album

    fun getAlbums(paramString: String, limit: Int): List<Album>

    fun getSongsForAlbum(albumId: Long, caller: String?): List<Song>

    fun getAlbumsForArtist(artistId: Long): List<Album>
}

class RealAlbumRepository(
    private val contentResolver: ContentResolver,
    private val sortOrderPref: Pref<AlbumSortOrder>

) : AlbumRepository {

    companion object {
        private const val SONG_TRACK_SORT_ORDER = ("$TRACK, $DEFAULT_SORT_ORDER")
    }

    override fun getAllAlbums(caller: String?): List<Album> {
        MediaID.currentCaller = caller
        return makeAlbumCursor(null, null)
                .mapList(true) { Album.fromCursor(this) }
    }

    override fun getAlbum(id: Long): Album {
        return getAlbum(makeAlbumCursor("_id=?", arrayOf(id.toString())))
    }

    override fun getAlbums(paramString: String, limit: Int): List<Album> {
        val result = makeAlbumCursor("album LIKE ?", arrayOf("$paramString%"))
                .mapList(true) { Album.fromCursor(this) }
        if (result.size < limit) {
            val moreResults = makeAlbumCursor("album LIKE ?", arrayOf("%_$paramString%"))
                    .mapList(true) { Album.fromCursor(this) }
            result += moreResults
        }
        return if (result.size < limit) {
            result
        } else {
            result.subList(0, limit)
        }
    }

    override fun getSongsForAlbum(albumId: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeAlbumSongCursor(albumId)
                .mapList(true) { Song.fromCursor(this, albumId = albumId) }
    }

    override fun getAlbumsForArtist(artistId: Long): List<Album> {
        return makeAlbumForArtistCursor(artistId)
                .mapList(true) { Album.fromCursor(this, artistId) }
    }

    private fun getAlbum(cursor: Cursor?): Album {
        return cursor?.use {
            if (cursor.moveToFirst()) {
                Album.fromCursor(cursor)
            } else {
                null
            }
        } ?: Album()
    }

    private fun makeAlbumCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return contentResolver.query(
                ALBUMS_URI,
                arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"),
                selection,
                paramArrayOfString,
                sortOrderPref.get().rawValue
        )
    }

    private fun makeAlbumForArtistCursor(artistID: Long): Cursor? {
        if (artistID == -1L) {
            return null
        }
        return contentResolver.query(
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistID),
                arrayOf("_id", "album", "artist", "numsongs", "minyear"),
                null,
                null,
                FIRST_YEAR
        )
    }

    private fun makeAlbumSongCursor(albumID: Long): Cursor? {
        val selection = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(
                SONGS_URI,
                arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"),
                selection,
                null,
                SONG_TRACK_SORT_ORDER
        )
    }
}

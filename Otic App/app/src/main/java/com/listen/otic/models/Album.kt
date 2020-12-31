
package com.listen.otic.models

import android.database.Cursor
import android.provider.MediaStore.Audio.Albums.ALBUM
import android.provider.MediaStore.Audio.Albums.ARTIST
import android.provider.MediaStore.Audio.Albums.FIRST_YEAR
import android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS
import android.provider.MediaStore.Audio.Albums._ID
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.listen.otic.OticAudioService.Companion.TYPE_ALBUM
import com.listen.otic.extensions.value
import com.listen.otic.extensions.valueOrEmpty
import com.listen.otic.util.Utils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
        var id: Long = 0,
        var title: String = "",
        var author: String = "",
        var authorId: Long = 0,
        var chapterCount: Int = 0,
        var year: Int = 0
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
                .setMediaId(MediaID(TYPE_ALBUM.toString(), id.toString()).asString())
                .setTitle(title)
                .setIconUri(Utils.getAlbumArtUri(id))
                .setSubtitle(author)
                .build(), FLAG_BROWSABLE) {

    companion object {
        fun fromCursor(cursor: Cursor, authorId: Long = -1): Album {
            return Album(
                    id = cursor.value(_ID),
                    title = cursor.valueOrEmpty(ALBUM),
                    author = cursor.valueOrEmpty(ARTIST),
                    authorId = authorId,
                    chapterCount = cursor.value(NUMBER_OF_SONGS),
                    year = cursor.value(FIRST_YEAR)
            )
        }
    }
}

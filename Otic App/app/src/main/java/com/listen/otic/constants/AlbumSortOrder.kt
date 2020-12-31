package com.listen.otic.constants

import android.provider.MediaStore

enum class AlbumSortOrder(val rawValue: String) {
    /* Album sort order A-Z */
    ALBUM_A_Z(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER),
    /* Album sort order Z-A */
    ALBUM_Z_A(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER + " DESC"),
    /* Album sort order songs */
    ALBUM_NUMBER_OF_SONGS(MediaStore.Audio.Albums.NUMBER_OF_SONGS + " DESC"),
    /* Album sort order year */
    ALBUM_YEAR(MediaStore.Audio.Albums.FIRST_YEAR + " DESC");

    companion object {
        fun fromString(raw: String): AlbumSortOrder {
            return AlbumSortOrder.values().single { it.rawValue == raw }
        }

        fun toString(value: AlbumSortOrder): String = value.rawValue
    }
}

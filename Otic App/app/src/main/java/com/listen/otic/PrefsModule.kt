package com.listen.otic

import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.listen.otic.constants.AlbumSortOrder
import com.listen.otic.constants.AlbumSortOrder.ALBUM_A_Z
import com.listen.otic.constants.AppThemes
import com.listen.otic.constants.AppThemes.LIGHT
import com.listen.otic.constants.SongSortOrder
import com.listen.otic.constants.SongSortOrder.SONG_A_Z
import com.listen.otic.constants.StartPage
import com.listen.otic.constants.StartPage.SONGS
import org.koin.dsl.module.module

const val PREF_APP_THEME = "theme_preference"
const val PREF_SONG_SORT_ORDER = "song_sort_order"
const val PREF_ALBUM_SORT_ORDER = "album_sort_order"
const val PREF_START_PAGE = "start_page_preference"
const val PREF_LAST_FOLDER = "last_folder"

val prefsModule = module {
    single { rxkPrefs(get()) }

    factory(name = PREF_SONG_SORT_ORDER) {
        get<RxkPrefs>().enum(PREF_SONG_SORT_ORDER, SONG_A_Z,
                SongSortOrder.Companion::fromString, SongSortOrder.Companion::toString)
    }

    factory(name = PREF_ALBUM_SORT_ORDER) {
        get<RxkPrefs>().enum(PREF_ALBUM_SORT_ORDER, ALBUM_A_Z,
                AlbumSortOrder.Companion::fromString, AlbumSortOrder.Companion::toString)
    }

    factory(name = PREF_APP_THEME) {
        get<RxkPrefs>().enum(PREF_APP_THEME, LIGHT,
                AppThemes.Companion::fromString, AppThemes.Companion::toString)
    }

    factory(name = PREF_START_PAGE) {
        get<RxkPrefs>().enum(PREF_START_PAGE, SONGS,
                StartPage.Companion::fromString, StartPage.Companion::toString)
    }

    factory(name = PREF_LAST_FOLDER) {
        val defaultFolder = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).path
        get<RxkPrefs>().string(PREF_LAST_FOLDER, defaultFolder)
    }
}

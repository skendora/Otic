package com.listen.otic.constants

enum class StartPage(val index: Int) {
    SONGS(0),
    ALBUMS(1),
    PLAYLISTS(2),
    ARTISTS(3),
    FOLDERS(4),
    GENRES(5);

    companion object {
        fun fromString(raw: String): StartPage {
            return StartPage.values().single { it.name.toLowerCase() == raw }
        }

        fun fromIndex(index: Int): StartPage {
            return StartPage.values().single { it.index == index }
        }

        fun toString(value: StartPage): String = value.name.toLowerCase()
    }
}

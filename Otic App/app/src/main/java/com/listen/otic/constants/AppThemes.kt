package com.listen.otic.constants

import com.listen.otic.R

enum class AppThemes(val rawValue: String, val themeRes: Int) {
    LIGHT("light", R.style.AppTheme_Light),
    DARK("dark", R.style.AppTheme_Dark),
    BLACK("black", R.style.AppTheme_Black);

    companion object {
        fun fromString(raw: String): AppThemes {
            return AppThemes.values().single { it.rawValue == raw }
        }

        fun toString(value: AppThemes): String = value.rawValue
    }
}

package com.listen.otic.extensions

import android.content.Context
import android.util.DisplayMetrics.DENSITY_DEFAULT

fun Float.dpToPixels(context: Context): Int {
    val resources = context.resources
    val metrics = resources.displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DENSITY_DEFAULT)).toInt()
}

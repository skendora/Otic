package com.listen.otic.extensions

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private var toast: Toast? = null

fun Context?.toast(@StringRes message: Int) {
    if (this == null) {
        return
    }
    toast?.cancel()
    toast = Toast.makeText(this, message, LENGTH_SHORT)
            .apply {
                show()
            }
}

fun Fragment.drawable(@DrawableRes res: Int): Drawable? {
    val context = activity ?: return null
    return context.drawable(res)
}

fun Activity?.drawable(@DrawableRes res: Int): Drawable? {
    if (this == null) {
        return null
    }
    return ContextCompat.getDrawable(this, res)
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.systemService(name: String): T {
    return getSystemService(name) as T
}

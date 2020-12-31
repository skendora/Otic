package com.listen.otic.extensions

import java.io.Closeable

fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (_: Throwable) {
    }
}

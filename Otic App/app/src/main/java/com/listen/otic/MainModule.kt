
package com.listen.otic

import android.app.Application
import android.content.ComponentName
import android.content.ContentResolver
import org.koin.dsl.module.module

val mainModule = module {

    factory<ContentResolver> {
        get<Application>().contentResolver
    }

    single {
        val component = ComponentName(get(), OticAudioService::class.java)
        RealMediaSessionConnection(get(), component)
    } bind MediaSessionConnection::class
}

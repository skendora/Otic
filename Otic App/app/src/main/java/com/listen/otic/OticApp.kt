
@file:Suppress("unused")

package com.listen.otic

import android.app.Application
import com.listen.otic.BuildConfig.DEBUG
import com.listen.otic.db.roomModule
import com.listen.otic.logging.FabricTree
import com.listen.otic.network.lastFmModule
import com.listen.otic.network.lyricsModule
import com.listen.otic.network.networkModule
import com.listen.otic.notifications.notificationModule
import com.listen.otic.repository.repositoriesModule
import com.listen.otic.ui.viewmodels.viewModelsModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class OticApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FabricTree())

        val modules = listOf(
                mainModule,
                prefsModule,
                networkModule,
                roomModule,
                notificationModule,
                repositoriesModule,
                viewModelsModule,
                lyricsModule,
                lastFmModule
        )
        startKoin(
                androidContext = this,
                modules = modules
        )
    }
}

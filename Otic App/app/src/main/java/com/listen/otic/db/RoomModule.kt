package com.listen.otic.db

import android.app.Application
import androidx.room.Room
import org.koin.dsl.module.module

val roomModule = module {

    single {
        Room.databaseBuilder(get<Application>(), OticDB::class.java, "queue.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    factory { get<OticDB>().queueDao() }

    factory {
        RealQueueHelper(get(), get())
    } bind QueueHelper::class
}

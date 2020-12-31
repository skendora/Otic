package com.listen.otic.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [QueueEntity::class, SongEntity::class], version = 2)
abstract class OticDB : RoomDatabase() {

    abstract fun queueDao(): QueueDao
}

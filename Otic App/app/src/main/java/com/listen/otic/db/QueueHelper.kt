package com.listen.otic.db

import com.listen.otic.extensions.equalsBy
import com.listen.otic.extensions.toSongEntityList
import com.listen.otic.repository.SongsRepository
import com.listen.otic.util.doAsync

interface QueueHelper {

    fun updateQueueSongs(
        queueSongs: LongArray?,
        currentSongId: Long?
    )

    fun updateQueueData(queueData: QueueEntity)
}

class RealQueueHelper(
    private val queueDao: QueueDao,
    private val songsRepository: SongsRepository
) : QueueHelper {

    override fun updateQueueSongs(queueSongs: LongArray?, currentSongId: Long?) {
        if (queueSongs == null || currentSongId == null) {
            return
        }
        // TODO replace with coroutines
        doAsync {
            val currentList = queueDao.getQueueSongsSync()
            val songListToSave = queueSongs.toSongEntityList(songsRepository)

            val listsEqual = currentList.equalsBy(songListToSave) { left, right ->
                left.id == right.id
            }
            if (queueSongs.isNotEmpty() && !listsEqual) {
                queueDao.clearQueueSongs()
                queueDao.insertAllSongs(songListToSave)
                setCurrentSongId(currentSongId)
            } else {
                setCurrentSongId(currentSongId)
            }
        }.execute()
    }

    override fun updateQueueData(queueData: QueueEntity) = queueDao.insert(queueData)

    private fun setCurrentSongId(id: Long) = queueDao.setCurrentId(id)
}

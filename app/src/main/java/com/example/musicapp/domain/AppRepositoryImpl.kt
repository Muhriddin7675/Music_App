package com.example.musicapp.domain

import com.example.musicapp.data.local.room.dao.MusicDao
import com.example.musicapp.data.local.room.entity.MusicEntity
import com.example.musicapp.util.myLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val musicDao: MusicDao
) : AppRepository {
    override fun getFavoriteMusic(): Flow<List<MusicEntity>> = callbackFlow {
        val list = musicDao.getAllBooksFromLocal()
        "${list.size} Repository".myLog()
        trySend(list)
        awaitClose()
    }.flowOn(Dispatchers.IO)

    override fun deleteFavoriteMusic(data: MusicEntity) {
        musicDao.deleteBooks(data)
    }

    override fun addFavoriteMusic(data: MusicEntity) {
        musicDao.insertBooks(data = data)
    }

    override fun isHasMusic(id: Int): Flow<Boolean> = callbackFlow {
        val result = musicDao.isHasMusic(id)
        trySend(result)
        awaitClose()
    }.flowOn(Dispatchers.IO)


}
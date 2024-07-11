package com.example.musicapp.domain

import com.example.musicapp.data.local.room.entity.MusicEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getFavoriteMusic(): Flow<List<MusicEntity>>
    fun deleteFavoriteMusic(data:MusicEntity)
    fun addFavoriteMusic(data: MusicEntity)
    fun isHasMusic(id:Int):Flow<Boolean>
}
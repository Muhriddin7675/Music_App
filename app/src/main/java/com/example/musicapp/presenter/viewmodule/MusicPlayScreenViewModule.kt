package com.example.musicapp.presenter.viewmodule

import com.example.musicapp.data.local.room.entity.MusicEntity
import kotlinx.coroutines.flow.Flow

interface MusicPlayScreenViewModule {
    val isHasMusicFavorite:Flow<Boolean>
    fun addFavoriteMusic(data: MusicEntity)
    fun deleteFavoriteMusic(data: MusicEntity)
    fun isHasMusic(id:Int)
}
package com.example.musicapp.presenter.viewmodule

import com.example.musicapp.data.local.room.entity.MusicEntity
import kotlinx.coroutines.flow.SharedFlow

interface MusicFavoriteScreenViewModule {
    val setFavoriteMusic:SharedFlow<List<MusicEntity>>
    fun getFavoriteMusic()
}
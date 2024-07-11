package com.example.musicapp.presenter.viewmodule.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.room.entity.MusicEntity
import com.example.musicapp.domain.AppRepository
import com.example.musicapp.domain.AppRepositoryImpl
import com.example.musicapp.presenter.viewmodule.MusicFavoriteScreenViewModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MusicFavoriteScreenViewModuleImpl @Inject constructor(
    private val repository: AppRepository
) : ViewModel(),
    MusicFavoriteScreenViewModule {
    override val setFavoriteMusic = MutableSharedFlow<List<MusicEntity>>()

    override fun getFavoriteMusic() {
        repository.getFavoriteMusic()
            .onEach {
                setFavoriteMusic.emit(it)
            }.launchIn(viewModelScope)
    }

}
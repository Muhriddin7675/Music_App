package com.example.musicapp.presenter.viewmodule.impl

import android.view.View
import android.view.ViewManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.room.entity.MusicEntity
import com.example.musicapp.domain.AppRepository
import com.example.musicapp.presenter.viewmodule.MusicPlayScreenViewModule
import com.example.musicapp.util.myLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.reflect.KProperty

@HiltViewModel
class MusicPlayScreenViewModuleImpl @Inject constructor(
    private val repository: AppRepository
) : ViewModel(), MusicPlayScreenViewModule {
    override val isHasMusicFavorite =  MutableSharedFlow<Boolean>()

    override fun addFavoriteMusic(data: MusicEntity) {
        repository.addFavoriteMusic(data)
    }
    override fun deleteFavoriteMusic(data: MusicEntity) {
        repository.deleteFavoriteMusic(data)
    }

    override fun isHasMusic(id: Int) {
        repository.isHasMusic(id).onEach {
            "$it ViewModule".myLog()
            isHasMusicFavorite.emit(it)
        }.launchIn(viewModelScope)

    }

}
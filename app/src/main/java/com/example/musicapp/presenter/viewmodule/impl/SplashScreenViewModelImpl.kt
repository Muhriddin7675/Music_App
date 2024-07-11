package com.example.musicapp.presenter.viewmodule.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.presenter.viewmodule.SplashScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModelImpl @Inject constructor():ViewModel(),SplashScreenViewModel {
    override val openMusicLibraryScreen  = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)

    override fun openLibraryScreen() {
        viewModelScope.launch {
            delay(1000)
            openMusicLibraryScreen.emit(Unit)
        }
    }

}
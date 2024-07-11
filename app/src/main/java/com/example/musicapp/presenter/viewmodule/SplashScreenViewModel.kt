package com.example.musicapp.presenter.viewmodule

import kotlinx.coroutines.flow.Flow

interface SplashScreenViewModel {
    val openMusicLibraryScreen: Flow<Unit>
    fun  openLibraryScreen()

}
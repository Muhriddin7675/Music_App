package com.example.musicapp.presenter.viewmodule.impl

import androidx.lifecycle.ViewModel
import com.example.musicapp.presenter.viewmodule.MusicLibraryScreenViewModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicLibraryScreenViewModuleImpl @Inject constructor():ViewModel(),MusicLibraryScreenViewModule {

}
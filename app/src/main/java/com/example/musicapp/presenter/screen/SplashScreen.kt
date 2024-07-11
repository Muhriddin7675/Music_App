package com.example.musicapp.presenter.screen

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.databinding.ScreenSplashBinding
import com.example.musicapp.presenter.viewmodule.SplashScreenViewModel
import com.example.musicapp.presenter.viewmodule.impl.SplashScreenViewModelImpl
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.checkPermissions
import com.example.musicapp.util.getMusicCursor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SplashScreen : Fragment(R.layout.screen_splash) {
    private val binding by viewBinding(ScreenSplashBinding::bind)
    private val viewModel: SplashScreenViewModel by viewModels<SplashScreenViewModelImpl>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.openLibraryScreen()
//
        viewModel.openMusicLibraryScreen
            .onEach {
                requireActivity().checkPermissions(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {
                    requireContext().getMusicCursor().onEach {
                        MyAppManager.cursor = it
                        findNavController().navigate(SplashScreenDirections.actionSplashScreenToMainScreen())
                    }.launchIn(lifecycleScope)
                }
            }
            .launchIn(lifecycleScope)
    }
}
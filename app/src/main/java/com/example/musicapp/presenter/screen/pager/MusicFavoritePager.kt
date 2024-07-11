package com.example.musicapp.presenter.screen.pager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.ScreenFavoriteBinding
import com.example.musicapp.presenter.adapter.MenuScreenAdapter
import com.example.musicapp.presenter.adapter.MusicFavoriteAdapter
import com.example.musicapp.presenter.screen.MainScreen
import com.example.musicapp.presenter.screen.MainScreenDirections
import com.example.musicapp.presenter.service.MyService
import com.example.musicapp.presenter.viewmodule.MusicFavoriteScreenViewModule
import com.example.musicapp.presenter.viewmodule.impl.MusicFavoriteScreenViewModuleImpl
import com.example.musicapp.util.MyAppManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MusicFavoritePager : Fragment(R.layout.screen_favorite) {

    private val binding by viewBinding(ScreenFavoriteBinding::bind)
    private val adapter by lazy { MusicFavoriteAdapter() }
    private val viewModule: MusicFavoriteScreenViewModule by viewModels<MusicFavoriteScreenViewModuleImpl>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMusicLibrary.adapter = adapter
        binding.rvMusicLibrary.layoutManager = LinearLayoutManager(requireContext())
        viewModule.getFavoriteMusic()

        viewModule.setFavoriteMusic.onEach {
            adapter.submitList(it)
            binding.linearEmpty.isVisible = it.isEmpty()
        }.launchIn(lifecycleScope)

        adapter.setOnClickSendMusic {musicUri ->
            sendMusicFileToFriend(requireContext(),musicUri)
        }

        adapter.setOnClickMusicPosition {
            MyAppManager.currentTime = 0
            MyAppManager.selectMusicPos = it
            startMyService(action = ActionEnum.PLAY)
            findNavController().navigate(MainScreenDirections.actionMainScreenToMusicPlayScreen())
        }
        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)

    }

    @SuppressLint("NotifyDataSetChanged")
    private val playMusicObserver = Observer<MusicData>{
        adapter.notifyDataSetChanged()
    }
    private fun startMyService(action: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    fun sendMusicFileToFriend(context: Context, musicUri: Uri) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, musicUri)
            type = "audio/mp3"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
}
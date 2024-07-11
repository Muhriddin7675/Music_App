package com.example.musicapp.presenter.screen.pager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.ScreenLibraryBinding
import com.example.musicapp.presenter.adapter.MusicLibraryScreenAdapter
import com.example.musicapp.presenter.screen.MainScreen
import com.example.musicapp.presenter.screen.MainScreenDirections
import com.example.musicapp.presenter.service.MyService
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.myLog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicLibraryPager : Fragment(R.layout.screen_library) {
    private val binding by viewBinding(ScreenLibraryBinding::bind)
    private val adapter by lazy { MusicLibraryScreenAdapter()}
    private val mainScreen by lazy { MainScreen() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.rvMusicLibrary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMusicLibrary.adapter = adapter
        adapter.cursor = MyAppManager.cursor

        adapter.setOnClickMusicPosition {
            MyAppManager.currentTime = 0
            MyAppManager.selectMusicPos = it
            startMyService(action = ActionEnum.PLAY)
            findNavController().navigate(MainScreenDirections.actionMainScreenToMusicPlayScreen())
        }
        adapter.setOnClickSendMusic { musicUri ->
            sendMusicFileToFriend(requireContext(), musicUri)
        }

        mainScreen.clickNext {
            "Next click".myLog()
            adapter.cursor = MyAppManager.cursor
        }
        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)

    }

    private fun startMyService(action: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val playMusicObserver = Observer<MusicData>{
        adapter.notifyDataSetChanged()
    }

    fun sendMusicFileToFriend(context: Context, musicUri: Uri) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, musicUri)
            type = "audio/mp3" // Musiqa faylning MIME turi
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

}
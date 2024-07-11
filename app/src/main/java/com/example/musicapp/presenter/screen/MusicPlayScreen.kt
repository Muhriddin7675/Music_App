package com.example.musicapp.presenter.screen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData
import com.example.musicapp.data.local.room.entity.MusicEntity
import com.example.musicapp.databinding.ScreenPlayMusicBinding
import com.example.musicapp.presenter.service.MyService
import com.example.musicapp.presenter.viewmodule.MusicPlayScreenViewModule
import com.example.musicapp.presenter.viewmodule.impl.MusicPlayScreenViewModuleImpl
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.myLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

@AndroidEntryPoint
class MusicPlayScreen : Fragment(R.layout.screen_play_music) {
    private val binding by viewBinding(ScreenPlayMusicBinding::bind)
    private val viewModule: MusicPlayScreenViewModule by viewModels<MusicPlayScreenViewModuleImpl>()
    private var musicData: MusicEntity? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val favoriteOn = binding.favoriteOn
        val favoriteOff = binding.favoriteOff

        refreshBtn()
        binding.btnRefresh.setOnClickListener {
            if (MyAppManager.refresh == 0) MyAppManager.refresh = 1
            else MyAppManager.refresh = 0
            refreshBtn()
        }
        binding.btnMenu.setOnClickListener {
            findNavController().popBackStack()
        }
        viewModule.isHasMusicFavorite.onEach {
            it.toString() + " music player".myLog()
            favoriteOn.isVisible = !it
            favoriteOff.isVisible = it
        }.launchIn(lifecycleScope)

        favoriteOn.setOnClickListener {
            if (musicData != null) {
                viewModule.addFavoriteMusic(musicData!!)
                Toast.makeText(requireContext(), "Added to saved !", Toast.LENGTH_SHORT)
                    .show()
                isHasMusicFavorite()

            }
        }
        favoriteOff.setOnClickListener {
            if (musicData != null) {
                viewModule.deleteFavoriteMusic(musicData!!)
                Toast.makeText(requireContext(), "Removed from saved !", Toast.LENGTH_SHORT)
                    .show()
                isHasMusicFavorite()
            }
        }
        binding.btnNext.setOnClickListener {
            startMyService(ActionEnum.NEXT)
            isHasMusicFavorite()
        }
        binding.btnPrev.setOnClickListener {
            startMyService(ActionEnum.PREV)
            isHasMusicFavorite()
        }
        binding.btnPlay.setOnClickListener { startMyService(ActionEnum.MANAGE) }
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MyAppManager.currentTime = progress.toLong()
                    startMyService(ActionEnum.SEEKBAR)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
        MyAppManager.currentTimeLiveData.observe(viewLifecycleOwner, currentTimeObserver)

    }

    private fun isHasMusicFavorite() {
        if (musicData != null) {
            viewModule.isHasMusic(musicData!!.musicId)
        }
    }

    private val playMusicObserver = Observer<MusicData> {
        musicData = MusicEntity(
            it.id,
            MyAppManager.selectMusicPos,
            it.artist,
            it.title,
            it.data,
            it.duration,
            it.albumId
        )
        isHasMusicFavorite()
        binding.seekbar.max = MyAppManager.fullTime.toInt()
        binding.seekbar.progress = 0
        binding.musicName.text = it.title
        binding.musicAuthor.text = it.artist
        binding.textAudioPlayedTime.text = MyAppManager.currentTime.toString()
        binding.textAudioTime.text = updateTimer(it.duration)
        binding.textAudioPlayedTime.text = updateTimer(0L)
    }

    private val isPlayingObserver = Observer<Boolean> {
        if (it) binding.btnPlay.setImageResource(R.drawable.ic_pause)
        else binding.btnPlay.setImageResource(R.drawable.ic_play)
    }

    private val currentTimeObserver = Observer<Long> {
        binding.seekbar.progress = MyAppManager.currentTime.toInt()
        binding.textAudioPlayedTime.text = updateTimer(MyAppManager.currentTime)
    }

    private fun updateTimer(currentTime: Long): String {
        val hours = (currentTime / (1000 * 60 * 60)).toInt()
        val minutes = ((currentTime / (1000 * 60)) % 60).toInt()
        val seconds = ((currentTime / 1000) % 60).toInt()

        val time: String
        time = if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        return time
    }

    private fun startMyService(action: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    private fun refreshBtn() {
        binding.textBtnRefresh.isVisible = MyAppManager.refresh != 0

    }
}
package com.example.musicapp.presenter.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.MenuScreenBinding
import com.example.musicapp.presenter.adapter.MenuScreenAdapter
import com.example.musicapp.presenter.adapter.MusicFavoriteAdapter
import com.example.musicapp.presenter.adapter.MusicLibraryScreenAdapter
import com.example.musicapp.presenter.service.MyService
import com.example.musicapp.util.MyAppManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreen : Fragment(R.layout.menu_screen) {
    private val binding by viewBinding(MenuScreenBinding::bind)

    private var nextClick: ((Int) -> Unit)? = null
    private lateinit var adapter: MenuScreenAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = MenuScreenAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        setVisible(false)
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottomLibrary -> binding.viewPager.currentItem = 0
                R.id.bottomMyBooks -> binding.viewPager.currentItem = 1
            }
            return@setOnItemSelectedListener true
        }


        binding.musicView.setOnClickListener {
            findNavController().navigate(MainScreenDirections.actionMainScreenToMusicPlayScreen())
        }

//        binding.btnMusicNext.setOnClickListener {
//            startMyService(ActionEnum.NEXT)
//            nextClick?.invoke(1)
//        }
        binding.btnMusicPlay.setOnClickListener { startMyService(ActionEnum.MANAGE) }
        binding.btnNext.setOnClickListener { startMyService(ActionEnum.NEXT) }
        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
    }

    private fun startMyService(action: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    private val playMusicObserver = Observer<MusicData> { data ->
        binding.apply {
            setVisible(true)
            musicName.text = data.title
            musicAuthor.text = data.artist
        }
    }

    private val isPlayingObserver = Observer<Boolean> {
        if (it) binding.btnMusicPlay.setImageResource(R.drawable.ic_pause)
        else binding.btnMusicPlay.setImageResource(R.drawable.ic_play)
    }

    fun clickNext(block: ((Int) -> Unit)) {
        nextClick = block
    }

    fun setVisible(bool:Boolean){
        binding.apply {
            musicName.isVisible = bool
            musicAuthor.isVisible = bool
            musicView.isVisible = bool
            imageMain.isVisible = bool
            btnMusicPlay.isVisible = bool
            btnNext.isVisible = bool
            imageView.isVisible = bool

        }
    }
}
package com.example.musicapp.presenter.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicapp.presenter.screen.pager.MusicFavoritePager
import com.example.musicapp.presenter.screen.pager.MusicLibraryPager

class MenuScreenAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MusicLibraryPager()
        else -> MusicFavoritePager()
    }
}
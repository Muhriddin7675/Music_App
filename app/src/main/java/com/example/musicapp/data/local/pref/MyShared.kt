package com.example.musicapp.data.local.pref

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyShared @Inject constructor(
    private val pref: SharedPreferences
) {
    fun setHasIntroPage(position: Int) {
        pref.edit().putInt("AdapterPosition", position).apply()
    }

    fun getHasIntroPage() = pref.getInt("AdapterPosition", 0)
}
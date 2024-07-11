package com.example.musicapp.util

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData

object MyAppManager {
    var selectMusicPos: Int = -1
    var cursor: Cursor? = null
    var lastCommand: ActionEnum = ActionEnum.PLAY

    var refresh:Int = 0

    var currentTime : Long = 0L
    var fullTime : Long = 0L

    val currentTimeLiveData = MutableLiveData<Long>()

    val playMusicLiveData = MutableLiveData<MusicData>()
    val isPlayingLiveData = MutableLiveData<Boolean>()
}
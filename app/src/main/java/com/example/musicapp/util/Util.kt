package com.example.musicapp.util

import android.util.Log
import android.widget.SeekBar

fun SeekBar.setChangeProgress(block: (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if(fromUser){
                MyAppManager.currentTime = progress.toLong()
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let { block.invoke(it.progress) }
        }
    })
}


fun String.myLog()= Log.d("TTT",this)

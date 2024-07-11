package com.example.musicapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MusicData (
    val id : Int,
    val artist:String?,
    val title:String?,
    val data:String?,
    val duration:Long,
    val albumId :Int
): Parcelable
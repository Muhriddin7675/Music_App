package com.example.musicapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookTable")
data class MusicEntity(
    @PrimaryKey
    val musicId : Int,
    val adapterPos: Int,
    val artist:String?,
    val title:String?,
    val data:String?,
    val duration:Long,
    val albumId :Int
)
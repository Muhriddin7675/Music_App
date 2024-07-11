package com.example.musicapp.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.room.entity.MusicEntity

@Dao
interface MusicDao {
    @Query("SELECT * FROM bookTable")
    fun getAllBooksFromLocal(): List<MusicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(data: MusicEntity)

    @Delete
    fun deleteBooks(data: MusicEntity)

    @Query("Select musicId From bookTable where musicId = :musicId")
    fun isHasMusic(musicId:Int):Boolean

}
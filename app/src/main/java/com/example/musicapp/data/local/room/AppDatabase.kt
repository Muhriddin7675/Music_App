package com.example.musicapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.room.dao.MusicDao
import com.example.musicapp.data.local.room.entity.MusicEntity
import javax.inject.Singleton

@Singleton
@Database(entities = [MusicEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMusicDao(): MusicDao
}
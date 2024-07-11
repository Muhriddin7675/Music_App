package com.example.musicapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.musicapp.data.local.room.AppDatabase
import com.example.musicapp.data.local.room.dao.MusicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharedPreferencesModule {
    @[Provides Singleton]
    fun providePref(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("MusicApp", Context.MODE_PRIVATE)

    @[Provides Singleton]
    fun providesBookDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "MyMusic.dp")
            .allowMainThreadQueries()
            .build()

    @[Provides Singleton]
    fun provideMusicDao(database: AppDatabase): MusicDao = database.getMusicDao()
}
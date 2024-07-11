package com.example.musicapp.di

import com.example.musicapp.domain.AppRepository
import com.example.musicapp.domain.AppRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppRepositoryModule {
    @Binds
    fun getRepository(impl: AppRepositoryImpl): AppRepository
}
package com.example.autotallerapp.di

import com.example.autotallerapp.data.repository.AsistenteIaRepositoryImpl
import com.example.autotallerapp.data.repository.HistorialChatRepositoryImpl
import com.example.autotallerapp.domain.repository.AsistenteIaRepository
import com.example.autotallerapp.domain.repository.HistorialChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AsistenteModule {
    @Binds
    @Singleton
    abstract fun enlazarHistorialChatRepository(impl: HistorialChatRepositoryImpl): HistorialChatRepository

    @Binds
    @Singleton
    abstract fun enlazarAsistenteIaRepository(impl: AsistenteIaRepositoryImpl): AsistenteIaRepository
}
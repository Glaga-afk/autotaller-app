package com.example.autotallerapp.di

import com.example.autotallerapp.data.repository.TecnicoRepositoryImpl
import com.example.autotallerapp.domain.repository.TecnicoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TecnicoModule {

    @Binds
    @Singleton
    abstract fun bindTecnicoRepository(
        tecnicoRepositoryImpl: TecnicoRepositoryImpl
    ): TecnicoRepository
}
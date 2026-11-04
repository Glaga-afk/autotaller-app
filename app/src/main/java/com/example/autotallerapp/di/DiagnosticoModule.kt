package com.example.autotallerapp.di

import com.example.autotallerapp.data.repository.DiagnosticoIaRepositoryImpl
import com.example.autotallerapp.data.repository.DiagnosticoRepositoryImpl
import com.example.autotallerapp.domain.repository.DiagnosticoIaRepository
import com.example.autotallerapp.domain.repository.DiagnosticoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticoModule {

    @Binds
    @Singleton
    abstract fun enlazarDiagnosticoRepository(impl: DiagnosticoRepositoryImpl): DiagnosticoRepository

    @Binds
    @Singleton
    abstract fun enlazarDiagnosticoIaRepository(impl: DiagnosticoIaRepositoryImpl): DiagnosticoIaRepository
}
package com.example.autotallerapp.di

import com.example.autotallerapp.data.repository.ClienteRepositoryImpl
import com.example.autotallerapp.domain.repository.ClienteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindClienteRepository(impl: ClienteRepositoryImpl): ClienteRepository
}
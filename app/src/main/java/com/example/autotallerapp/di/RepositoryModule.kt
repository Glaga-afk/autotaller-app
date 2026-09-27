package com.example.autotallerapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.autotallerapp.data.repository.AuthRepositoryImpl
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun enlazarAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

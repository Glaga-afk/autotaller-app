package com.example.autotallerapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.autotallerapp.data.repository.AuthRepositoryImpl
import com.example.autotallerapp.data.repository.AutomovilRepositoryImpl
import com.example.autotallerapp.data.repository.BitacoraRepositoryImpl
import com.example.autotallerapp.data.repository.CatalogoRepositoryImpl
import com.example.autotallerapp.domain.repository.AuthRepository
import com.example.autotallerapp.domain.repository.AutomovilRepository
import com.example.autotallerapp.domain.repository.BitacoraRepository
import com.example.autotallerapp.domain.repository.CatalogoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun enlazarAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun enlazarAutomovilRepository(impl: AutomovilRepositoryImpl): AutomovilRepository

    @Binds
    @Singleton
    abstract fun enlazarCatalogoRepository(impl: CatalogoRepositoryImpl): CatalogoRepository

    @Binds
    @Singleton
    abstract fun enlazarBitacoraRepository(impl: BitacoraRepositoryImpl): BitacoraRepository
}

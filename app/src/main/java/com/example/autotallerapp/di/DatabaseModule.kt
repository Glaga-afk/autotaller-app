package com.example.autotallerapp.di

import android.content.Context
import androidx.room.Room
import com.example.autotallerapp.data.local.AutoTallerDatabase
import com.example.autotallerapp.data.local.CatalogoSeed
import com.example.autotallerapp.data.local.dao.MarcaDao
import com.example.autotallerapp.data.local.dao.ModeloDao
import com.example.autotallerapp.data.local.dao.ProblemaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun proveerBaseDeDatos(@ApplicationContext contexto: Context): AutoTallerDatabase {
        val db = Room.databaseBuilder(contexto, AutoTallerDatabase::class.java, "autotaller.db").build()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            CatalogoSeed.sembrarSiVacio(db)
        }
        return db
    }

    @Provides
    fun proveerMarcaDao(db: AutoTallerDatabase): MarcaDao = db.marcaDao()

    @Provides
    fun proveerModeloDao(db: AutoTallerDatabase): ModeloDao = db.modeloDao()

    @Provides
    fun proveerProblemaDao(db: AutoTallerDatabase): ProblemaDao = db.problemaDao()
}
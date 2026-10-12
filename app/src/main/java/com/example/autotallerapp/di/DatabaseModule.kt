package com.example.autotallerapp.di

import android.content.Context
import androidx.room.Room
import com.example.autotallerapp.data.local.dao.ClienteDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClienteDao(@ApplicationContext context: Context): ClienteDao {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "autotaller_db"
        ).fallbackToDestructiveMigration(true).build()
        return db.clienteDao()
    }
}
package com.example.autotallerapp.di

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.autotallerapp.data.local.dao.ClienteDao
import com.example.autotallerapp.data.local.entity.ClienteEntity

@Database(entities = [ClienteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
}
package com.example.autotallerapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.autotallerapp.data.local.dao.MarcaDao
import com.example.autotallerapp.data.local.dao.ModeloDao
import com.example.autotallerapp.data.local.entity.MarcaEntity
import com.example.autotallerapp.data.local.entity.ModeloEntity

@Database(
    entities = [MarcaEntity::class, ModeloEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AutoTallerDatabase : RoomDatabase() {
    abstract fun marcaDao(): MarcaDao
    abstract fun modeloDao(): ModeloDao
}
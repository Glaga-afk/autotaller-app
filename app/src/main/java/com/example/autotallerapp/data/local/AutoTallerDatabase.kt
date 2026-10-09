package com.example.autotallerapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.autotallerapp.data.local.dao.MarcaDao
import com.example.autotallerapp.data.local.dao.ModeloDao
import com.example.autotallerapp.data.local.dao.ProblemaDao
import com.example.autotallerapp.data.local.entity.MarcaEntity
import com.example.autotallerapp.data.local.entity.ModeloEntity
import com.example.autotallerapp.data.local.entity.ProblemaEntity

@Database(
    entities = [MarcaEntity::class, ModeloEntity::class, ProblemaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AutoTallerDatabase : RoomDatabase() {
    abstract fun marcaDao(): MarcaDao
    abstract fun modeloDao(): ModeloDao
    abstract fun problemaDao(): ProblemaDao
}
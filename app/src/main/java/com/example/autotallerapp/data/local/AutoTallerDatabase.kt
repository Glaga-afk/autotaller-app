package com.example.autotallerapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.autotallerapp.data.local.dao.ClienteDao
import com.example.autotallerapp.data.local.dao.MarcaDao
import com.example.autotallerapp.data.local.dao.ModeloDao
import com.example.autotallerapp.data.local.dao.OrdenDao
import com.example.autotallerapp.data.local.dao.ProblemaDao
import com.example.autotallerapp.data.local.dao.TecnicoDao
import com.example.autotallerapp.data.local.entity.ClienteEntity
import com.example.autotallerapp.data.local.entity.MarcaEntity
import com.example.autotallerapp.data.local.entity.ModeloEntity
import com.example.autotallerapp.data.local.entity.OrdenEntity
import com.example.autotallerapp.data.local.entity.ProblemaEntity
import com.example.autotallerapp.data.local.entity.TecnicoEntity

@Database(
    entities = [
        MarcaEntity::class,
        ModeloEntity::class,
        ProblemaEntity::class,
        ClienteEntity::class,
        OrdenEntity::class,
        TecnicoEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class AutoTallerDatabase : RoomDatabase() {
    abstract fun marcaDao(): MarcaDao
    abstract fun modeloDao(): ModeloDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun ordenDao(): OrdenDao
    abstract fun tecnicoDao(): TecnicoDao
}
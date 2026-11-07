package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.autotallerapp.data.local.entity.TecnicoEntity

@Dao
interface TecnicoDao {

    @Query("SELECT * FROM tecnicos WHERE disponible = 1")
    suspend fun obtenerDisponiblesLocales(): List<TecnicoEntity>

    @Query("SELECT * FROM tecnicos")
    suspend fun obtenerTodosLocales(): List<TecnicoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(tecnicos: List<TecnicoEntity>)
}
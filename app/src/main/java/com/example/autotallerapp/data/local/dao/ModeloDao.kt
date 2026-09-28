package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.autotallerapp.data.local.entity.ModeloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeloDao {
    @Query("SELECT * FROM modelos WHERE marcaId = :marcaId ORDER BY nombre ASC")
    fun obtenerPorMarca(marcaId: Int): Flow<List<ModeloEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(modelos: List<ModeloEntity>)

    @Query("SELECT COUNT(*) FROM modelos")
    suspend fun contar(): Int
}
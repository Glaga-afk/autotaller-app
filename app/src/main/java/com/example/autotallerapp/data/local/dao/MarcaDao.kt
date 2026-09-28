package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.autotallerapp.data.local.entity.MarcaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcaDao {
    @Query("SELECT * FROM marcas ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<MarcaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(marcas: List<MarcaEntity>)

    @Query("SELECT COUNT(*) FROM marcas")
    suspend fun contar(): Int

}
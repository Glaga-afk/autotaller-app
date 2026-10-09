package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.autotallerapp.data.local.entity.ProblemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemaDao {
    @Query("""
        SELECT * FROM problemas_fts
        WHERE problemas_fts MATCH :consulta
        AND (:sistema = '' OR sistema = :sistema)
        AND (:marca = '' OR marca = :marca)
        AND (:modelo = '' OR modelo = :modelo)
        ORDER BY titulo ASC
    """)
    fun buscarConTexto(
        consulta: String,
        sistema: String,
        marca: String,
        modelo: String
    ): Flow<List<ProblemaEntity>>

    @Query("""
        SELECT * FROM problemas_fts
        WHERE (:sistema = '' OR sistema = :sistema)
        AND (:marca = '' OR marca = :marca)
        AND (:modelo = '' OR modelo = :modelo)
        ORDER BY titulo ASC
    """)
    fun buscarSinTexto(
        sistema: String,
        marca: String,
        modelo: String
    ): Flow<List<ProblemaEntity>>

    @Query("SELECT COUNT(*) FROM problemas_fts")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(problemas: List<ProblemaEntity>)

    @Query("DELETE FROM problemas_fts")
    suspend fun limpiar()

    @Transaction
    suspend fun reemplazarTodo(problemas: List<ProblemaEntity>) {
        limpiar()
        insertarTodos(problemas)
    }
}
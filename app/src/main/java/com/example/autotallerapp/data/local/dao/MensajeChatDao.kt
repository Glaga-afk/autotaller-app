package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.autotallerapp.data.local.entity.MensajeChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeChatDao {
    @Query("SELECT * FROM mensajes_chat WHERE conversacionId = :conversacionId ORDER BY creadoEn ASC, id ASC")
    fun observar(conversacionId: String): Flow<List<MensajeChatEntity>>

    @Insert
    suspend fun insertar(mensaje: MensajeChatEntity)

    @Query("DELETE FROM mensajes_chat WHERE conversacionId = :conversacionId")
    suspend fun eliminarConversacion(conversacionId: String)
}
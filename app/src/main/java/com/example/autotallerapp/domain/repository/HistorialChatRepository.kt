package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.MensajeChat
import kotlinx.coroutines.flow.Flow

interface HistorialChatRepository {
    /** Room avisa cada vez que la conversacion cambia. */
    fun observar(conversacionId: String): Flow<List<MensajeChat>>
    suspend fun guardar(conversacionId: String, mensaje: MensajeChat): Resultado<Unit>
    suspend fun borrar(conversacionId: String): Resultado<Unit>
}
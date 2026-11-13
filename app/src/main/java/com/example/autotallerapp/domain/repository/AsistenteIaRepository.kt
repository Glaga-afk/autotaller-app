package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico

interface AsistenteIaRepository {
    /**
     * Responde la consulta del tecnico.
     * [orden] es el contexto (null = consulta general), [casos] salen de la bitacora
     * y [historial] son los mensajes anteriores de la conversacion.
     */
    suspend fun responder(
        orden: OrdenServicio?,
        casos: List<ProblemaTecnico>,
        historial: List<MensajeChat>,
        consulta: String
    ): Resultado<String>
}
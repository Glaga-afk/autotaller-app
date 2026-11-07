package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.AsignacionTecnico
import com.example.autotallerapp.domain.model.Tecnico

interface TecnicoRepository {
    /** Consulta en Firestore los usuarios registrados con rol "Técnico" o "TECNICO". */
    suspend fun obtenerTecnicosDisponibles(): Resultado<List<Tecnico>>

    /** Registra la lista de técnicos asignados a la orden en Firestore y actualiza el estado a EN_REPARACION. */
    suspend fun asignarTecnicosAOrden(
        idOrdenLocal: String,
        asignaciones: List<AsignacionTecnico>
    ): Resultado<Unit>
}
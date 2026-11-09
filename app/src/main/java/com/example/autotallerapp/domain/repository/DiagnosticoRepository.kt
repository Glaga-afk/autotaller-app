package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.DiagnosticoOrden
import com.example.autotallerapp.domain.model.OrdenConEvidencia
import com.example.autotallerapp.domain.model.OrdenServicio

interface DiagnosticoRepository {
    suspend fun obtenerOrdenesRecientes(): Resultado<List<OrdenServicio>>
    suspend fun obtenerOrdenConFotos(ordenId: String): Resultado<OrdenConEvidencia>
    suspend fun guardar(diagnostico: DiagnosticoOrden): Resultado<Unit>
}
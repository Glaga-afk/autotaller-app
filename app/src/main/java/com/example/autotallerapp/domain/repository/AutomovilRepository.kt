package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Automovil

interface AutomovilRepository {
    suspend fun existePlaca(placa: String): Boolean
    suspend fun registrar(automovil: Automovil): Resultado<Automovil>
    suspend fun obtenerPorCliente(clienteId: String): Resultado<List<Automovil>>
}
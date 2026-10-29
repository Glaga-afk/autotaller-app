package com.example.autotallerapp.domain.repository
import com.example.autotallerapp.domain.model.Cliente
import kotlinx.coroutines.flow.Flow
interface ClienteRepository {
    suspend fun buscarClientePorDni(dni: String): Result<Cliente?>
    suspend fun registrarCliente(cliente: Cliente): Result<Unit>
    fun obtenerClientes(): Flow<List<Cliente>>
}
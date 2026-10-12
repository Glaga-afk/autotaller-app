package com.example.autotallerapp.domain.repository
import com.example.autotallerapp.domain.model.Cliente
interface ClienteRepository {
    suspend fun buscarClientePorDni(dni: String): Result<Cliente?>
    suspend fun registrarCliente(cliente: Cliente): Result<Unit>
}
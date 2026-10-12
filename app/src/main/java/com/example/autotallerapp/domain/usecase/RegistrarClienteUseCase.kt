package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.repository.ClienteRepository
import javax.inject.Inject

class RegistrarClienteUseCase @Inject constructor(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(cliente: Cliente): Result<Unit> {
        if (cliente.dni.trim().length != 8 || !cliente.dni.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("El DNI debe ser de 8 dígitos."))
        }
        if (cliente.nombreCompleto.isBlank() || cliente.direccion.isBlank() || cliente.telefono.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos los campos son obligatorios."))
        }
        val existe = repository.buscarClientePorDni(cliente.dni)
        if (existe.isSuccess && existe.getOrNull() != null) {
            return Result.failure(IllegalStateException("El cliente con DNI ${cliente.dni} ya se encuentra registrado."))
        }
        return repository.registrarCliente(cliente)
    }
}
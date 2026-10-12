package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.repository.ClienteRepository
import javax.inject.Inject

class RegistrarClienteUseCase @Inject constructor(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(cliente: Cliente): Result<Unit> {
        val dni = cliente.dni.trim()

        if (dni.length != 8 || !dni.all { it.isDigit() }) {
            return Result.failure(
                IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos.")
            )
        }

        val clienteLimpio = cliente.copy(
            dni = dni,
            nombreCompleto = cliente.nombreCompleto.trim(),
            direccion = cliente.direccion.trim(),
            sexo = cliente.sexo.trim(),
            telefono = cliente.telefono.trim()
        )

        if (
            clienteLimpio.nombreCompleto.isBlank() ||
            clienteLimpio.direccion.isBlank() ||
            clienteLimpio.sexo.isBlank() ||
            clienteLimpio.telefono.isBlank()
        ) {
            return Result.failure(
                IllegalArgumentException("Todos los campos son obligatorios.")
            )
        }

        val consulta = repository.buscarClientePorDni(dni)

        if (consulta.isFailure) {
            return Result.failure(
                consulta.exceptionOrNull()
                    ?: Exception("No se pudo verificar el DNI.")
            )
        }

        if (consulta.getOrNull() != null) {
            return Result.failure(
                IllegalStateException(
                    "El cliente con DNI $dni ya se encuentra registrado."
                )
            )
        }

        return repository.registrarCliente(clienteLimpio)
    }
}
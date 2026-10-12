package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.repository.ClienteRepository
import javax.inject.Inject

class BuscarClientePorDniUseCase @Inject constructor(
    private val repository: ClienteRepository
) {
    suspend operator fun invoke(dni: String): Result<Cliente?> {
        val dniLimpio = dni.trim()
        if (dniLimpio.length != 8 || !dniLimpio.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos."))
        }
        return repository.buscarClientePorDni(dniLimpio)
    }
}
package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Automovil
import com.example.autotallerapp.domain.repository.AutomovilRepository
import javax.inject.Inject

class ObtenerAutomovilesClienteUseCase @Inject constructor (
    private val repository: AutomovilRepository
) {
    suspend operator fun invoke(clienteId: String): Resultado<List<Automovil>> =
        repository.obtenerPorCliente(clienteId)
}
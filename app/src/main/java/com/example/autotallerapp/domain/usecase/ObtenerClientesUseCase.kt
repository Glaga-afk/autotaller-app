package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerClientesUseCase @Inject constructor(
    private val clienteRepository: ClienteRepository
) {
    operator fun invoke(): Flow<List<Cliente>> {
        return clienteRepository.obtenerClientes()
    }
}
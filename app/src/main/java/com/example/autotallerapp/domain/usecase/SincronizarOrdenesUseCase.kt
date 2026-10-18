package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

class SincronizarOrdenesUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    suspend operator fun invoke(): Resultado<Int> = repository.sincronizarPendientes()
}

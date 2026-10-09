package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.repository.BitacoraRepository
import javax.inject.Inject

class SincronizarBitacoraUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(): Resultado<Int> = repository.sincronizar()
}
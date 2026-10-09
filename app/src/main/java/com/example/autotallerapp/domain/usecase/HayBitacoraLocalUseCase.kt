package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.repository.BitacoraRepository
import javax.inject.Inject

class HayBitacoraLocalUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(): Boolean = repository.hayDatosLocales()
}
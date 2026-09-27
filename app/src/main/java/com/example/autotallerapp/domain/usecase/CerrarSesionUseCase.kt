package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class CerrarSesionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.cerrarSesion()
}

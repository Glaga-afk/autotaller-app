package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.core.Validaciones
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class RecuperarPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(correo: String): Resultado<Unit> {
        Validaciones.errorCorreo(correo)?.let { return Resultado.Error(it) }
        return repository.enviarCorreoRecuperacion(correo.trim())
    }
}

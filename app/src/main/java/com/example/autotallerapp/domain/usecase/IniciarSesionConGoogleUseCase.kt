package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionConGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Resultado<Usuario> =
        repository.iniciarSesionConGoogle(idToken)
}

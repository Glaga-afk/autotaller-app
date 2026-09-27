package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.core.Validaciones
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(correo: String, password: String): Resultado<Usuario> {
        if (!Validaciones.correoValido(correo)) {
            return Resultado.Error("El correo no tiene un formato valido")
        }
        if (password.isBlank()) {
            return Resultado.Error("Ingresa tu contrasena")
        }
        return repository.iniciarSesion(correo.trim(), password)
    }
}

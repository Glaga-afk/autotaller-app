package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.core.Validaciones
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegistrarUsuarioUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        nombre: String,
        correo: String,
        password: String,
        confirmacion: String
    ): Resultado<Usuario> {
        Validaciones.errorNombre(nombre)?.let { return Resultado.Error(it) }
        Validaciones.errorCorreo(correo)?.let { return Resultado.Error(it) }
        Validaciones.errorPassword(password)?.let { return Resultado.Error(it) }
        Validaciones.errorConfirmacion(password, confirmacion)?.let { return Resultado.Error(it) }
        return repository.registrar(nombre.trim(), correo.trim(), password)
    }
}

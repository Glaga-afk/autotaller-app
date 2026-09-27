package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject

class ObtenerSesionActivaUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Usuario? {
        val uid = repository.uidSesionActiva() ?: return null
        return when (val resultado = repository.obtenerUsuario(uid)) {
            is Resultado.Exito -> resultado.dato
            is Resultado.Error -> null
        }
    }
}

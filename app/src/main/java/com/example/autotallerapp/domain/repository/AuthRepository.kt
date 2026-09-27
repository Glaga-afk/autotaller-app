package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Usuario

interface AuthRepository {

    fun uidSesionActiva(): String?

    suspend fun iniciarSesion(correo: String, password: String): Resultado<Usuario>

    suspend fun registrar(nombre: String, correo: String, password: String): Resultado<Usuario>

    suspend fun iniciarSesionConGoogle(idToken: String): Resultado<Usuario>

    suspend fun enviarCorreoRecuperacion(correo: String): Resultado<Unit>

    suspend fun obtenerUsuario(uid: String): Resultado<Usuario>

    fun cerrarSesion()
}

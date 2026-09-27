package com.example.autotallerapp.core

import android.util.Patterns

object Validaciones {

    fun correoValido(correo: String): Boolean =
        correo.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()

    fun errorCorreo(correo: String): String? = when {
        correo.isBlank() -> "Ingresa tu correo"
        !correoValido(correo) -> "El correo no tiene un formato valido"
        else -> null
    }

    fun errorPassword(password: String): String? = when {
        password.isBlank() -> "Ingresa tu contrasena"
        password.length < 8 -> "Debe tener al menos 8 caracteres"
        !password.any { it.isUpperCase() } -> "Debe incluir al menos una mayuscula"
        !password.any { it.isDigit() } -> "Debe incluir al menos un numero"
        else -> null
    }

    fun errorNombre(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa tu nombre completo"
        nombre.trim().length < 5 -> "Ingresa el nombre completo"
        else -> null
    }

    fun errorConfirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contrasena"
        password != confirmacion -> "Las contrasenas no coinciden"
        else -> null
    }
}

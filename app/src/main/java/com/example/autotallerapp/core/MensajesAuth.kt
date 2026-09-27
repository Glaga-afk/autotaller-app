package com.example.autotallerapp.core

import com.google.firebase.auth.FirebaseAuthException

object MensajesAuth {

    fun traducir(e: Exception): String {
        if (e !is FirebaseAuthException) {
            return e.message ?: "Ocurrio un error inesperado. Intentalo de nuevo"
        }
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato valido"
            "ERROR_USER_NOT_FOUND",
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> "Correo o contrasena incorrectos"
            "ERROR_USER_DISABLED" -> "Tu cuenta esta desactivada. Contacta al administrador"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya esta registrado"
            "ERROR_WEAK_PASSWORD" -> "La contrasena es muy debil"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera unos minutos"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexion. Revisa tu internet"
            else -> "No se pudo completar la operacion. Intentalo de nuevo"
        }
    }
}

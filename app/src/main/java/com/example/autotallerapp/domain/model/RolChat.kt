package com.example.autotallerapp.domain.model

enum class RolChat {
    USUARIO,
    ASISTENTE;

    companion object {
        fun desde(valor: String?): RolChat =
            entries.firstOrNull { it.name == valor } ?: USUARIO
    }
}
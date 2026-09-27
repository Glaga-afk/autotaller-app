package com.example.autotallerapp.domain.model

enum class Rol(val etiqueta: String) {
    RECEPCIONISTA("Recepcionista"),
    TECNICO_SUPERVISOR("Tecnico supervisor"),
    TECNICO("Tecnico"),
    ADMINISTRADOR("Administrador"),
    PENDIENTE("Pendiente de asignacion");

    companion object {
        fun desde(valor: String?): Rol =
            entries.firstOrNull { it.name == valor?.uppercase() } ?: PENDIENTE
    }
}

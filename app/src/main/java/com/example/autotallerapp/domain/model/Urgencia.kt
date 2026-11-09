package com.example.autotallerapp.domain.model

enum class Urgencia(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    CRITICA("Critica");

    companion object {
        fun desde(valor: String?): Urgencia =
            entries.firstOrNull { it.name == valor?.uppercase() } ?: MEDIA
    }
}
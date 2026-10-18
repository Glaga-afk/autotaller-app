package com.example.autotallerapp.domain.model

/** Tipos de evidencia que se adjuntan a una orden de servicio. */
enum class TipoArchivo(val extension: String, val mime: String) {
    FOTO("jpg", "image/jpeg"),
    AUDIO("m4a", "audio/mp4")
}

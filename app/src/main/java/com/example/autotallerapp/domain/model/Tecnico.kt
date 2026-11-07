package com.example.autotallerapp.domain.model

data class Tecnico(
    val id: String = "",
    val nombre: String = "",
    val especialidad: String = "", // ej: "Electricidad", "Mecánica general", "Frenos"
    val disponible: Boolean = true
)

data class AsignacionTecnico(
    val tecnicoId: String = "",
    val nombreTecnico: String = "",
    val tareaAsignada: String = "", // Detalle del trabajo a realizar
    val fechaAsignacion: Long = System.currentTimeMillis()
)
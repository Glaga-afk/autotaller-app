package com.example.autotallerapp.domain.model

data class ProblemaTecnico(
    val id: String = "",
    val sistema: SistemaAutomovil = SistemaAutomovil.OTRO,
    val marca: String = "",
    val modelo: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val solucion: String = "",
    val actualizadoEn: Long = System.currentTimeMillis()
)

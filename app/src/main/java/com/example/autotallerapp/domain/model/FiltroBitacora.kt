package com.example.autotallerapp.domain.model

data class FiltroBitacora(
    val palabraClave: String = "",
    val sistema: SistemaAutomovil? = null,
    val marca: String = "",
    val modelo: String = ""
)

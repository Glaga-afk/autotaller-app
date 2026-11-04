package com.example.autotallerapp.domain.model

data class SugerenciaDiagnostico(
    val sistema: SistemaAutomovil,
    val causaProbable: String,
    val urgencia: Urgencia,
    val palabrasClave: List<String> = emptyList()
)

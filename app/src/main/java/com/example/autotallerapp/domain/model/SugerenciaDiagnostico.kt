package com.example.autotallerapp.domain.model

/** Lo que devuelve la IA. Reutiliza SistemaAutomovil de la bitacora (HU04). */
data class SugerenciaDiagnostico(
    val sistema: SistemaAutomovil,
    val causaProbable: String,
    val urgencia: Urgencia,
    val palabrasClave: List<String> = emptyList()
)

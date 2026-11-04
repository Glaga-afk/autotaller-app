package com.example.autotallerapp.domain.model

data class DiagnosticoOrden(
    val ordenId: String,
    val sistema: SistemaAutomovil,
    val causaProbable: String,
    val urgencia: Urgencia,
    val sugerenciaIa: SugerenciaDiagnostico,
    val corregidoPorSupervisor: Boolean
)

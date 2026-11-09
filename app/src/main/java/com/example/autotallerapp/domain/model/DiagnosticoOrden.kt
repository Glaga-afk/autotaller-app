package com.example.autotallerapp.domain.model

/** Diagnostico final vinculado a la OST. Guarda tambien lo que sugirio la IA. */
data class DiagnosticoOrden(
    val ordenId: String,
    val sistema: SistemaAutomovil,
    val causaProbable: String,
    val urgencia: Urgencia,
    val sugerenciaIa: SugerenciaDiagnostico,
    val corregidoPorSupervisor: Boolean
)

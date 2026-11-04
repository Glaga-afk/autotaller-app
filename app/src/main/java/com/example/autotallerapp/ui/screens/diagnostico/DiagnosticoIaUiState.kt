package com.example.autotallerapp.ui.screens.diagnostico

import com.example.autotallerapp.domain.model.FotoEvidencia
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.SugerenciaDiagnostico
import com.example.autotallerapp.domain.model.Urgencia

data class DiagnosticoIaUiState(
    val cargandoOrden: Boolean = true,
    val errorOrden: String? = null,
    val orden: OrdenServicio? = null,
    val fotos: List<FotoEvidencia> = emptyList(),

    val analizando: Boolean = false,
    val errorIa: String? = null,
    val sugerencia: SugerenciaDiagnostico? = null,

    // Lo que el supervisor puede corregir antes de guardar
    val sistema: SistemaAutomovil? = null,
    val causa: String = "",
    val urgencia: Urgencia? = null,

    val casosSimilares: List<ProblemaTecnico> = emptyList(),
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null
) {
    /** True si el supervisor cambio algo de lo que sugirio la IA. */
    val corregido: Boolean
        get() = sugerencia != null && (
                sistema != sugerencia.sistema ||
                        causa.trim() != sugerencia.causaProbable.trim() ||
                        urgencia != sugerencia.urgencia
                )
}

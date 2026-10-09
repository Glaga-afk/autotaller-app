package com.example.autotallerapp.ui.screens.bitacora

import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil

data class BitacoraUiState(
    val palabraClave: String = "",
    val sistemaSeleccionado: SistemaAutomovil? = null,
    val marca: String = "",
    val modelo: String = "",
    val filtrosVisibles: Boolean = false,
    val resultados: List<ProblemaTecnico> = emptyList(),
    val cargando: Boolean = true,
    val sincronizando: Boolean = false,
    val bitacoraVacia: Boolean = false,
    val mensaje: String? = null
) {
    val hayFiltrosActivos: Boolean
        get() = palabraClave.isNotBlank() || sistemaSeleccionado != null || marca.isNotBlank() || modelo.isNotBlank()
}

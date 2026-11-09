package com.example.autotallerapp.ui.screens.diagnostico

import com.example.autotallerapp.domain.model.OrdenServicio

data class SeleccionOrdenUiState(
    val ordenes: List<OrdenServicio> = emptyList(),
    val cargando: Boolean = true,
    val mensajeError: String? = null
)

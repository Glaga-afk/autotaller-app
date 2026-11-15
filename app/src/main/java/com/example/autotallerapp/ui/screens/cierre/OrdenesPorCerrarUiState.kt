package com.example.autotallerapp.ui.screens.cierre

import com.example.autotallerapp.domain.model.OrdenServicio

data class OrdenesPorCerrarUiState(
    val cargando: Boolean = true,
    val ordenes: List<OrdenServicio> = emptyList(),
    val error: String? = null
)

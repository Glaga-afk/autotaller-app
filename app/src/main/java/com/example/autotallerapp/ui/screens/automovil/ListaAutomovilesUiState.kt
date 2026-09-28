package com.example.autotallerapp.ui.screens.automovil

import com.example.autotallerapp.domain.model.Automovil

data class ListaAutomovilesUiState(
    val automoviles: List<Automovil> = emptyList(),
    val cargando: Boolean = true,
    val mensajeError: String? = null
)

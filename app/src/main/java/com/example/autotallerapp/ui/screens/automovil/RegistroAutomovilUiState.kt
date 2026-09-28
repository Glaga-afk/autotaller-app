package com.example.autotallerapp.ui.screens.automovil

import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.model.Modelo

data class RegistroAutomovilUiState(
    val placa: String = "",
    val marcas: List<Marca> = emptyList(),
    val modelos: List<Modelo> = emptyList(),
    val marcaSeleccionada: Marca? = null,
    val modeloSeleccionado: Modelo? = null,
    val anio: String = "",
    val color: String = "",
    val errorPlaca: String? = null,
    val errorMarca: String? = null,
    val errorModelo: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false
)

sealed interface RegistroAutomovilEvento {
    data object AutomovilRegistrado : RegistroAutomovilEvento
}
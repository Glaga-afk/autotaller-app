package com.example.autotallerapp.ui.screens.orden

import com.example.autotallerapp.domain.model.Automovil
import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.Ubicacion
import com.example.autotallerapp.domain.usecase.RegistrarOrdenUseCase

data class NuevaOrdenUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val cliente: Cliente? = null,
    val automovil: Automovil? = null,
    val falla: String = "",
    val fotos: List<String> = emptyList(),
    val audio: String? = null,
    val grabando: Boolean = false,
    val segundosGrabados: Int = 0,
    val reproduciendo: Boolean = false,
    val incluirUbicacion: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val ubicacion: Ubicacion? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val ordenRegistrada: OrdenServicio? = null
) {
    val puedeAgregarFoto: Boolean get() = fotos.size < RegistrarOrdenUseCase.MAX_FOTOS
}

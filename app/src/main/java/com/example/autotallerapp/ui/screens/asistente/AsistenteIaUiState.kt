package com.example.autotallerapp.ui.screens.asistente

import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.model.OrdenServicio

data class AsistenteIaUiState(
    // Contexto elegido (null = consulta general) y su conversacion
    val orden: OrdenServicio? = null,
    val mensajes: List<MensajeChat> = emptyList(),

    val entrada: String = "",
    val respondiendo: Boolean = false,
    val errorIa: String? = null,
    /** Pregunta que se envio y aun no tiene respuesta: sirve para reintentar. */
    val ultimaConsulta: String? = null,

    val escuchando: Boolean = false,
    val vozDisponible: Boolean = true,

    // Selector de orden
    val mostrarSelectorOrden: Boolean = false,
    val cargandoOrdenes: Boolean = false,
    val ordenesDisponibles: List<OrdenServicio> = emptyList(),
    val errorOrdenes: String? = null,

    val mensaje: String? = null
)

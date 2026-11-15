package com.example.autotallerapp.ui.screens.cierre

import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.TrabajoTecnico

data class InformeTecnicoUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,

    // Datos de la OST (criterio 1)
    val orden: OrdenServicio? = null,
    val solucion: String = "",

    // Tecnicos y trabajo realizado (criterio 2)
    val trabajos: List<TrabajoTecnico> = emptyList(),

    // Problema nuevo para la bitacora (criterio 4)
    val registrarProblema: Boolean = false,
    val sistema: SistemaAutomovil = SistemaAutomovil.OTRO,
    val tituloProblema: String = "",
    val causaProblema: String = "",
    val similares: List<ProblemaTecnico> = emptyList(),

    // Cierre (criterio 3)
    val confirmandoCierre: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val cerrada: Boolean = false,
    val problemaRegistrado: Boolean = false
)

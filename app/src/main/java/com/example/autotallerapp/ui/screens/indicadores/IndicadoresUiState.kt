package com.example.autotallerapp.ui.screens.indicadores

import com.example.autotallerapp.domain.model.EstadoOrden

enum class FiltroTiempo {
    HISTORICO,
    ESTE_MES,
    ESTA_SEMANA
}

data class IndicadoresUiState(
    val cargando: Boolean = false,
    val totalOrdenes: Int = 0,
    val ordenesPorEstado: Map<EstadoOrden, Int> = emptyMap(),
    val fallasFrecuentes: List<Pair<String, Int>> = emptyList(),
    val filtroSeleccionado: FiltroTiempo = FiltroTiempo.HISTORICO,
    val error: String? = null
)

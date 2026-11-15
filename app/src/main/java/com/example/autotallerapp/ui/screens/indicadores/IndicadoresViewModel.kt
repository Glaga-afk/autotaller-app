package com.example.autotallerapp.ui.screens.indicadores

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import com.example.autotallerapp.work.ProgramadorSincronizacion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class IndicadoresViewModel @Inject constructor(
    @param:ApplicationContext private val contexto: Context? = null,
    private val ordenRepository: OrdenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndicadoresUiState())
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    private var todasLasOrdenes = listOf<OrdenServicio>()

    init {
        contexto?.let {
            ProgramadorSincronizacion.programarCalculoIndicadores(it)
        }
        cargarIndicadores()
    }

    fun cargarIndicadores() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            // 1. Offline-First: Cargar localmente primero
            when (val resultadoLocal = ordenRepository.obtenerLocales()) {
                is Resultado.Exito -> {
                    todasLasOrdenes = resultadoLocal.dato
                    aplicarFiltroYProcesar(_uiState.value.filtroSeleccionado)
                }
                is Resultado.Error -> {}
            }

            // 2. Sincronizar con la nube
            when (val resultadoRemoto = ordenRepository.obtenerTodas()) {
                is Resultado.Exito -> {
                    todasLasOrdenes = resultadoRemoto.dato
                    aplicarFiltroYProcesar(_uiState.value.filtroSeleccionado)
                }
                is Resultado.Error -> {
                    if (todasLasOrdenes.isEmpty()) {
                        _uiState.update { it.copy(cargando = false, error = resultadoRemoto.mensaje) }
                    } else {
                        _uiState.update { it.copy(cargando = false) }
                    }
                }
            }
        }
    }

    fun cambiarFiltro(filtro: FiltroTiempo) {
        _uiState.update { it.copy(filtroSeleccionado = filtro) }
        aplicarFiltroYProcesar(filtro)
    }

    private fun aplicarFiltroYProcesar(filtro: FiltroTiempo) {
        val ahora = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val ordenesFiltradas = when (filtro) {
            FiltroTiempo.HISTORICO -> todasLasOrdenes
            FiltroTiempo.ESTE_MES -> {
                calendar.timeInMillis = ahora
                val mesActual = calendar.get(Calendar.MONTH)
                val anioActual = calendar.get(Calendar.YEAR)
                todasLasOrdenes.filter { orden ->
                    calendar.timeInMillis = orden.fechaRegistro
                    calendar.get(Calendar.MONTH) == mesActual && calendar.get(Calendar.YEAR) == anioActual
                }
            }
            FiltroTiempo.ESTA_SEMANA -> {
                calendar.timeInMillis = ahora
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                val inicioSemana = calendar.timeInMillis
                todasLasOrdenes.filter {	it.fechaRegistro >= inicioSemana }
            }
        }

        val total = ordenesFiltradas.size
        val porEstado = ordenesFiltradas.groupingBy { it.estado }.eachCount()
        val frecuencias = ordenesFiltradas
            .map { it.falla.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        _uiState.update {
            it.copy(
                cargando = false,
                totalOrdenes = total,
                ordenesPorEstado = porEstado,
                fallasFrecuentes = frecuencias
            )
        }
    }
}

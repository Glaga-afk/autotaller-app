package com.example.autotallerapp.ui.screens.orden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.AsignacionTecnico
import com.example.autotallerapp.domain.model.Tecnico
import com.example.autotallerapp.domain.repository.TecnicoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AsignarTecnicoUiState {
    object Cargando : AsignarTecnicoUiState
    data class Exito(val tecnicos: List<Tecnico>) : AsignarTecnicoUiState
    data class Error(val mensaje: String) : AsignarTecnicoUiState
}

@HiltViewModel
class AsignarTecnicoViewModel @Inject constructor(
    private val tecnicoRepository: TecnicoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AsignarTecnicoUiState>(AsignarTecnicoUiState.Cargando)
    val uiState: StateFlow<AsignarTecnicoUiState> = _uiState.asStateFlow()

    // Estado de los técnicos seleccionados y sus tareas: Map<TecnicoId, TareaAsignada>
    private val _asignacionesTemporales = MutableStateFlow>(emptyMap())
    val asignacionesTemporales: StateFlow> = _asignacionesTemporales.asStateFlow()

    init {
        cargarTecnicos()
    }

    fun cargarTecnicos() {
        viewModelScope.launch {
            _uiState.value = AsignarTecnicoUiState.Cargando
            when (val resultado = tecnicoRepository.obtenerTecnicosDisponibles()) {
                is Resultado.Exito -> _uiState.value = AsignarTecnicoUiState.Exito(resultado.dato)
                is Resultado.Error -> _uiState.value = AsignarTecnicoUiState.Error(resultado.mensaje)
            }
        }
    }

    fun actualizarTareaTecnico(tecnicoId: String, tarea: String) {
        _asignacionesTemporales.update { actual ->
            if (tarea.isBlank()) actual - tecnicoId else actual + (tecnicoId to tarea)
        }
    }

    fun confirmarAsignacion(
        idOrdenLocal: String,
        listaTecnicosDisponibles: List<Tecnico>,
        onAsignacionExitosa: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val asignaciones = _asignacionesTemporales.value.mapNotNull { (tecnicoId, tarea) ->
                val tecnico = listaTecnicosDisponibles.find { it.id == tecnicoId }
                tecnico?.let {
                    AsignacionTecnico(
                        tecnicoId = it.id,
                        nombreTecnico = it.nombre,
                        tareaAsignada = tarea
                    )
                }
            }

            if (asignaciones.isEmpty()) {
                onError("Debe seleccionar al menos un técnico y especificar su tarea.")
                return@launch
            }

            when (val resultado = tecnicoRepository.asignarTecnicosAOrden(idOrdenLocal, asignaciones)) {
                is Resultado.Exito -> onAsignacionExitosa()
                is Resultado.Error -> onError(resultado.mensaje)
            }
        }
    }
}
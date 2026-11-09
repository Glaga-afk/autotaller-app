package com.example.autotallerapp.ui.screens.orden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import com.example.autotallerapp.domain.repository.TecnicoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListaOrdenesUiState(
    val cargando: Boolean = false,
    val ordenes: List<OrdenServicio> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ListaOrdenesViewModel @Inject constructor(
    private val ordenRepository: OrdenRepository,
    private val tecnicoRepository: TecnicoRepository // 👈 Inyectamos el repositorio para liberar a los técnicos
) : ViewModel() {

    private val _estado = MutableStateFlow(ListaOrdenesUiState())
    val estado: StateFlow<ListaOrdenesUiState> = _estado.asStateFlow()

    init {
        cargarOrdenes()
    }

    fun cargarOrdenes() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = ordenRepository.obtenerTodas()) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false, ordenes = resultado.dato) }
                }
                is Resultado.Error -> {
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
                }
            }
        }
    }

    // 🚀 Nueva función para finalizar/actualizar la orden y liberar a los técnicos asignados
    fun actualizarEstadoOrden(idOrdenLocal: String, nuevoEstado: EstadoOrden) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            when (val resultado = tecnicoRepository.actualizarEstadoOrdenYLiberarTecnicos(idOrdenLocal, nuevoEstado)) {
                is Resultado.Exito -> {
                    // Si se actualizó y liberó correctamente, recargamos la lista para reflejar los cambios
                    cargarOrdenes()
                }
                is Resultado.Error -> {
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
                }
            }
        }
    }
}
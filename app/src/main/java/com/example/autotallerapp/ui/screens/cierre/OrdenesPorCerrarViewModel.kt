package com.example.autotallerapp.ui.screens.cierre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.usecase.ObtenerOrdenesPorCerrarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** HU08: lista de OST en reparacion que el supervisor puede cerrar. */
@HiltViewModel
class OrdenesPorCerrarViewModel @Inject constructor(
    private val obtenerOrdenesPorCerrar: ObtenerOrdenesPorCerrarUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(OrdenesPorCerrarUiState())
    val estado: StateFlow<OrdenesPorCerrarUiState> = _estado.asStateFlow()

    /** Se llama cada vez que la pantalla aparece, para no mostrar una OST que ya se cerro. */
    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = obtenerOrdenesPorCerrar()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(cargando = false, ordenes = resultado.dato)
                }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}

package com.example.autotallerapp.ui.screens.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.usecase.ObtenerOrdenesRecientesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeleccionOrdenViewModel @Inject constructor(
    private val obtenerOrdenesRecientes: ObtenerOrdenesRecientesUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(SeleccionOrdenUiState())
    val estado: StateFlow<SeleccionOrdenUiState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            when (val resultado = obtenerOrdenesRecientes()) {
                is Resultado.Exito ->
                    _estado.update { it.copy(ordenes = resultado.dato, cargando = false) }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}
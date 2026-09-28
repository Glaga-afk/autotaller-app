package com.example.autotallerapp.ui.screens.automovil

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.usecase.ObtenerAutomovilesClienteUseCase
import javax.inject.Inject

@HiltViewModel
class ListaAutomovilesViewModel @Inject constructor(
    private val obtenerAutomoviles: ObtenerAutomovilesClienteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val clienteId: String = checkNotNull(savedStateHandle["clienteId"])

    private val _estado = MutableStateFlow(ListaAutomovilesUiState())
    val estado: StateFlow<ListaAutomovilesUiState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            when (val resultado = obtenerAutomoviles(clienteId)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        automoviles = resultado.dato.sortedByDescending { auto -> auto.creadoEn },
                        cargando = false
                    )
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}
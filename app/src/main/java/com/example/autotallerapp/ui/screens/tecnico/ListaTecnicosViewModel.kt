package com.example.autotallerapp.ui.screens.tecnico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Tecnico
import com.example.autotallerapp.domain.repository.TecnicoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ListaTecnicosUiState {
    object Cargando : ListaTecnicosUiState
    data class Exito(val tecnicos: List<Tecnico>) : ListaTecnicosUiState
    data class Error(val mensaje: String) : ListaTecnicosUiState
}

@HiltViewModel
class ListaTecnicosViewModel @Inject constructor(
    private val tecnicoRepository: TecnicoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListaTecnicosUiState>(ListaTecnicosUiState.Cargando)
    val uiState: StateFlow<ListaTecnicosUiState> = _uiState.asStateFlow()

    init {
        cargarTecnicos()
    }

    fun cargarTecnicos() {
        viewModelScope.launch {
            _uiState.value = ListaTecnicosUiState.Cargando
            when (val resultado = tecnicoRepository.obtenerTecnicosDisponibles()) {
                is Resultado.Exito -> {
                    _uiState.value = ListaTecnicosUiState.Exito(resultado.dato)
                }
                is Resultado.Error -> {
                    _uiState.value = ListaTecnicosUiState.Error(resultado.mensaje)
                }
            }
        }
    }
}
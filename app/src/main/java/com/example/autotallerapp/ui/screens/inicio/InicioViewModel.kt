package com.example.autotallerapp.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.usecase.CerrarSesionUseCase
import com.example.autotallerapp.domain.usecase.ObtenerSesionActivaUseCase
import javax.inject.Inject

data class InicioUiState(
    val usuario: Usuario? = null,
    val cargando: Boolean = true,
    val sesionCerrada: Boolean = false
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val obtenerSesionActiva: ObtenerSesionActivaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(InicioUiState())
    val estado: StateFlow<InicioUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerSesionActiva()
            _estado.update {
                it.copy(usuario = usuario, cargando = false, sesionCerrada = usuario == null)
            }
        }
    }

    fun cerrarSesion() {
        cerrarSesionUseCase()
        _estado.update { it.copy(usuario = null, sesionCerrada = true) }
    }
}

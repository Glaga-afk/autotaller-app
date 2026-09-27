package com.example.autotallerapp.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.autotallerapp.domain.model.Rol
import com.example.autotallerapp.domain.usecase.ObtenerSesionActivaUseCase
import javax.inject.Inject

sealed interface DestinoInicial {
    data object Cargando : DestinoInicial
    data object Login : DestinoInicial
    data object Inicio : DestinoInicial
    data object Pendiente : DestinoInicial
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val obtenerSesionActiva: ObtenerSesionActivaUseCase
) : ViewModel() {

    private val _destino = MutableStateFlow<DestinoInicial>(DestinoInicial.Cargando)
    val destino: StateFlow<DestinoInicial> = _destino.asStateFlow()

    init {
        viewModelScope.launch {
            delay(600)
            val usuario = obtenerSesionActiva()
            _destino.value = when {
                usuario == null -> DestinoInicial.Login
                usuario.rol == Rol.PENDIENTE -> DestinoInicial.Pendiente
                else -> DestinoInicial.Inicio
            }
        }
    }
}

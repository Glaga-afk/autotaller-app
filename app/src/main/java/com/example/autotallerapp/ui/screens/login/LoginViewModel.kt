package com.example.autotallerapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.core.Validaciones
import com.example.autotallerapp.domain.model.Rol
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.usecase.IniciarSesionConGoogleUseCase
import com.example.autotallerapp.domain.usecase.IniciarSesionUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val iniciarSesion: IniciarSesionUseCase,
    private val iniciarSesionConGoogle: IniciarSesionConGoogleUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private val _eventos = Channel<LoginEvento>()
    val eventos = _eventos.receiveAsFlow()

    fun alCambiarCorreo(valor: String) {
        _estado.update { it.copy(correo = valor, errorCorreo = null, mensajeError = null) }
    }

    fun alCambiarPassword(valor: String) {
        _estado.update { it.copy(password = valor, errorPassword = null, mensajeError = null) }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensajeError = null) }
    }

    fun alPulsarIngresar() {
        val actual = _estado.value
        val errorCorreo = Validaciones.errorCorreo(actual.correo)
        val errorPassword = if (actual.password.isBlank()) "Ingresa tu contrasena" else null

        if (errorCorreo != null || errorPassword != null) {
            _estado.update { it.copy(errorCorreo = errorCorreo, errorPassword = errorPassword) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            when (val resultado = iniciarSesion(actual.correo, actual.password)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false) }
                    emitirDestino(resultado.dato)
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }

    fun alObtenerIdTokenGoogle(idToken: String) {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoGoogle = true, mensajeError = null) }
            when (val resultado = iniciarSesionConGoogle(idToken)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargandoGoogle = false) }
                    emitirDestino(resultado.dato)
                }
                is Resultado.Error ->
                    _estado.update {
                        it.copy(cargandoGoogle = false, mensajeError = resultado.mensaje)
                    }
            }
        }
    }

    fun alFallarGoogle(mensaje: String) {
        _estado.update { it.copy(cargandoGoogle = false, mensajeError = mensaje) }
    }

    fun alIniciarGoogle() {
        _estado.update { it.copy(cargandoGoogle = true, mensajeError = null) }
    }

    private suspend fun emitirDestino(usuario: Usuario) {
        val evento =
            if (usuario.rol == Rol.PENDIENTE) LoginEvento.IrAPendiente else LoginEvento.IrAInicio
        _eventos.send(evento)
    }
}

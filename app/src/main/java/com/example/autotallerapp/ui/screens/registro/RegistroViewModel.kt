package com.example.autotallerapp.ui.screens.registro

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
import com.example.autotallerapp.domain.usecase.RegistrarUsuarioUseCase
import javax.inject.Inject

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorConfirmacion: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registrarUsuario: RegistrarUsuarioUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    private val _cuentaCreada = Channel<Unit>()
    val cuentaCreada = _cuentaCreada.receiveAsFlow()

    fun alCambiarNombre(valor: String) =
        _estado.update { it.copy(nombre = valor, errorNombre = null, mensajeError = null) }

    fun alCambiarCorreo(valor: String) =
        _estado.update { it.copy(correo = valor, errorCorreo = null, mensajeError = null) }

    fun alCambiarPassword(valor: String) = _estado.update {
        it.copy(
            password = valor,
            errorPassword = null,
            errorConfirmacion = if (it.confirmacion.isBlank()) null else it.errorConfirmacion,
            mensajeError = null
        )
    }

    fun alCambiarConfirmacion(valor: String) = _estado.update {
        it.copy(
            confirmacion = valor,
            errorConfirmacion = if (valor.isBlank() || valor == it.password) {
                null
            } else {
                "Las contrasenas no coinciden"
            },
            mensajeError = null
        )
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensajeError = null) }

    fun alPulsarCrearCuenta() {
        val actual = _estado.value
        val errores = RegistroUiState(
            errorNombre = Validaciones.errorNombre(actual.nombre),
            errorCorreo = Validaciones.errorCorreo(actual.correo),
            errorPassword = Validaciones.errorPassword(actual.password),
            errorConfirmacion = Validaciones.errorConfirmacion(actual.password, actual.confirmacion)
        )

        val hayErrores = listOfNotNull(
            errores.errorNombre,
            errores.errorCorreo,
            errores.errorPassword,
            errores.errorConfirmacion
        ).isNotEmpty()

        if (hayErrores) {
            _estado.update {
                it.copy(
                    errorNombre = errores.errorNombre,
                    errorCorreo = errores.errorCorreo,
                    errorPassword = errores.errorPassword,
                    errorConfirmacion = errores.errorConfirmacion
                )
            }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            val resultado = registrarUsuario(
                nombre = actual.nombre,
                correo = actual.correo,
                password = actual.password,
                confirmacion = actual.confirmacion
            )
            when (resultado) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false) }
                    _cuentaCreada.send(Unit)
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}

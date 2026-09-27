package com.example.autotallerapp.ui.screens.recuperar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.core.Validaciones
import com.example.autotallerapp.domain.usecase.RecuperarPasswordUseCase
import javax.inject.Inject

data class RecuperarUiState(
    val correo: String = "",
    val errorCorreo: String? = null,
    val mensajeError: String? = null,
    val enviado: Boolean = false,
    val cargando: Boolean = false
)

@HiltViewModel
class RecuperarViewModel @Inject constructor(
    private val recuperarPassword: RecuperarPasswordUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(RecuperarUiState())
    val estado: StateFlow<RecuperarUiState> = _estado.asStateFlow()

    fun alCambiarCorreo(valor: String) = _estado.update {
        it.copy(correo = valor, errorCorreo = null, mensajeError = null, enviado = false)
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensajeError = null) }

    fun alPulsarEnviar() {
        val actual = _estado.value
        Validaciones.errorCorreo(actual.correo)?.let { error ->
            _estado.update { it.copy(errorCorreo = error) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            when (val resultado = recuperarPassword(actual.correo)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, enviado = true) }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}

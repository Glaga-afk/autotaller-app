package com.example.autotallerapp.ui.screens.diagnostico

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.Urgencia
import com.example.autotallerapp.domain.usecase.ContrastarConBitacoraUseCase
import com.example.autotallerapp.domain.usecase.DiagnosticarConIAUseCase
import com.example.autotallerapp.domain.usecase.GuardarDiagnosticoUseCase
import com.example.autotallerapp.domain.usecase.ObtenerOrdenConFotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticoIAViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obtenerOrdenConFotos: ObtenerOrdenConFotosUseCase,
    private val diagnosticarConIa: DiagnosticarConIAUseCase,
    private val contrastarConBitacora: ContrastarConBitacoraUseCase,
    private val guardarDiagnostico: GuardarDiagnosticoUseCase
) : ViewModel() {

    private val ordenId: String = checkNotNull(savedStateHandle["ordenId"])

    private val _estado = MutableStateFlow(DiagnosticoIAUiState())
    val estado: StateFlow<DiagnosticoIAUiState> = _estado.asStateFlow()

    init {
        cargarOrden()
    }

    // ---------- Orden ----------

    fun cargarOrden() {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoOrden = true, errorOrden = null) }
            when (val resultado = obtenerOrdenConFotos(ordenId)) {
                is Resultado.Exito -> {
                    _estado.update {
                        it.copy(
                            cargandoOrden = false,
                            orden = resultado.dato.orden,
                            fotos = resultado.dato.fotos
                        )
                    }
                    diagnosticar() // el analisis arranca solo al abrir la orden
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargandoOrden = false, errorOrden = resultado.mensaje) }
            }
        }
    }

    // ---------- IA ----------

    fun diagnosticar() {
        val actual = _estado.value
        val orden = actual.orden ?: return
        if (actual.analizando) return

        viewModelScope.launch {
            _estado.update { it.copy(analizando = true, errorIa = null) }
            when (val resultado = diagnosticarConIa(orden, actual.fotos)) {
                is Resultado.Exito -> {
                    val sugerencia = resultado.dato
                    _estado.update {
                        it.copy(
                            analizando = false,
                            sugerencia = sugerencia,
                            sistema = sugerencia.sistema,
                            causa = sugerencia.causaProbable,
                            urgencia = sugerencia.urgencia
                        )
                    }
                    actualizarCasosSimilares()
                }
                is Resultado.Error ->
                    _estado.update { it.copy(analizando = false, errorIa = resultado.mensaje) }
            }
        }
    }

    // ---------- Correccion del supervisor ----------

    fun alElegirSistema(sistema: SistemaAutomovil) {
        _estado.update { it.copy(sistema = sistema) }
        actualizarCasosSimilares()
    }

    fun alCambiarCausa(texto: String) = _estado.update { it.copy(causa = texto) }

    fun alElegirUrgencia(urgencia: Urgencia) = _estado.update { it.copy(urgencia = urgencia) }

    /** Contraste con la bitacora local (HU04): se recalcula si el supervisor cambia el sistema. */
    private fun actualizarCasosSimilares() {
        val actual = _estado.value
        val orden = actual.orden ?: return
        val sistema = actual.sistema ?: return

        viewModelScope.launch {
            val casos = contrastarConBitacora(
                sistema = sistema,
                causaProbable = actual.causa,
                palabrasClave = actual.sugerencia?.palabrasClave.orEmpty(),
                marca = orden.marca,
                modelo = orden.modelo
            )
            _estado.update { it.copy(casosSimilares = casos) }
        }
    }

    // ---------- Guardar ----------

    fun guardar() {
        val actual = _estado.value
        val sugerencia = actual.sugerencia ?: return
        val sistema = actual.sistema ?: return
        val urgencia = actual.urgencia ?: return
        if (actual.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            when (val resultado = guardarDiagnostico(ordenId, sugerencia, sistema, actual.causa, urgencia)) {
                is Resultado.Exito -> _estado.update { it.copy(guardando = false, guardado = true) }
                is Resultado.Error ->
                    _estado.update { it.copy(guardando = false, mensaje = resultado.mensaje) }
            }
        }
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }
}
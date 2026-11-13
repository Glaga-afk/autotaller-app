package com.example.autotallerapp.ui.screens.asistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.device.ReconocedorVoz
import com.example.autotallerapp.domain.model.CONVERSACION_GENERAL
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.RolChat
import com.example.autotallerapp.domain.usecase.BorrarHistorialChatUseCase
import com.example.autotallerapp.domain.usecase.EnviarConsultaAsistenteUseCase
import com.example.autotallerapp.domain.usecase.ObservarHistorialChatUseCase
import com.example.autotallerapp.domain.usecase.ObtenerOrdenesRecientesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AsistenteIaViewModel @Inject constructor(
    private val observarHistorial: ObservarHistorialChatUseCase,
    private val enviarConsulta: EnviarConsultaAsistenteUseCase,
    private val borrarHistorial: BorrarHistorialChatUseCase,
    private val obtenerOrdenesRecientes: ObtenerOrdenesRecientesUseCase,
    private val reconocedor: ReconocedorVoz
) : ViewModel() {
    private val _estado = MutableStateFlow(AsistenteIaUiState())
    val estado: StateFlow<AsistenteIaUiState> = _estado.asStateFlow()

    /** Cada orden tiene su propia conversacion; sin orden se usa la general. */
    private val conversacionId = MutableStateFlow(CONVERSACION_GENERAL)

    init {
        _estado.update { it.copy(vozDisponible = reconocedor.estaDisponible()) }

        viewModelScope.launch {
            conversacionId
                .flatMapLatest { observarHistorial(it) }
                .collect { lista -> _estado.update { it.copy(mensajes = lista) } }
        }
    }

    // ---------- Contexto: orden elegida ----------

    fun abrirSelectorOrden() {
        _estado.update { it.copy(mostrarSelectorOrden = true) }
        cargarOrdenes()
    }

    fun cerrarSelectorOrden() = _estado.update { it.copy(mostrarSelectorOrden = false) }

    fun cargarOrdenes() {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoOrdenes = true, errorOrdenes = null) }
            when (val resultado = obtenerOrdenesRecientes()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        cargandoOrdenes = false,
                        // Las ordenes ya atendidas no necesitan ayuda del asistente
                        ordenesDisponibles = resultado.dato.filter { orden -> orden.estado != EstadoOrden.ATENDIDA }
                    )
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargandoOrdenes = false, errorOrdenes = resultado.mensaje) }
            }
        }
    }

    /** Cambia el contexto y, con el, la conversacion que se muestra. */
    fun elegirOrden(orden: OrdenServicio?) {
        reconocedor.cancelar()
        conversacionId.value = orden?.idLocal ?: CONVERSACION_GENERAL
        _estado.update {
            it.copy(
                orden = orden,
                mostrarSelectorOrden = false,
                entrada = "",
                escuchando = false,
                errorIa = null,
                ultimaConsulta = null
            )
        }
    }

    // ---------- Chat ----------

    fun alCambiarEntrada(texto: String) = _estado.update { it.copy(entrada = texto) }

    fun enviarSugerencia(texto: String) {
        _estado.update { it.copy(entrada = texto) }
        enviar()
    }

    fun enviar() {
        val texto = _estado.value.entrada.trim()
        if (texto.isEmpty()) return
        _estado.update { it.copy(entrada = "") }
        lanzarConsulta(texto, reintento = false)
    }

    fun reintentar() {
        val pendiente = _estado.value.ultimaConsulta ?: return
        lanzarConsulta(pendiente, reintento = true)
    }

    private fun lanzarConsulta(consulta: String, reintento: Boolean) {
        val actual = _estado.value
        if (actual.respondiendo) return
        reconocedor.cancelar()

        // En un reintento la pregunta ya esta guardada: no se duplica ni se manda dos veces al modelo
        val ultimo = actual.mensajes.lastOrNull()
        val yaGuardada = reintento && ultimo?.rol == RolChat.USUARIO && ultimo.texto == consulta
        val historialPrevio = if (yaGuardada) actual.mensajes.dropLast(1) else actual.mensajes
        val idConversacion = conversacionId.value

        viewModelScope.launch {
            _estado.update {
                it.copy(respondiendo = true, escuchando = false, errorIa = null, ultimaConsulta = consulta)
            }
            val resultado = enviarConsulta(
                conversacionId = idConversacion,
                orden = actual.orden,
                consulta = consulta,
                historialPrevio = historialPrevio,
                consultaYaGuardada = yaGuardada
            )
            when (resultado) {
                is Resultado.Exito ->
                    _estado.update { it.copy(respondiendo = false, ultimaConsulta = null) }
                is Resultado.Error ->
                    _estado.update { it.copy(respondiendo = false, errorIa = resultado.mensaje) }
            }
        }
    }

    fun borrarConversacion() {
        viewModelScope.launch {
            when (val resultado = borrarHistorial(conversacionId.value)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(errorIa = null, ultimaConsulta = null) }
                is Resultado.Error ->
                    _estado.update { it.copy(mensaje = resultado.mensaje) }
            }
        }
    }

    // ---------- Voz ----------

    fun iniciarEscucha() {
        val actual = _estado.value
        if (actual.escuchando || actual.respondiendo) return
        if (!reconocedor.estaDisponible()) {
            _estado.update {
                it.copy(vozDisponible = false, mensaje = "Este dispositivo no tiene reconocimiento de voz disponible")
            }
            return
        }

        _estado.update { it.copy(escuchando = true, errorIa = null) }
        reconocedor.iniciar(
            alParcial = { texto -> _estado.update { it.copy(entrada = texto) } },
            alFinal = { texto ->
                // Manos ocupadas: al terminar de hablar, la consulta se envia sola
                _estado.update { it.copy(escuchando = false, entrada = texto) }
                enviar()
            },
            alError = { mensaje ->
                _estado.update { it.copy(escuchando = false, mensaje = mensaje ?: it.mensaje) }
            }
        )
    }

    fun detenerEscucha() = reconocedor.detener()

    // ---------- Mensajes ----------

    fun mostrarMensaje(texto: String) = _estado.update { it.copy(mensaje = texto) }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }

    override fun onCleared() {
        reconocedor.cancelar()
        super.onCleared()
    }
}
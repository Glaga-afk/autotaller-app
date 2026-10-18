package com.example.autotallerapp.ui.screens.orden

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.device.GrabadorAudio
import com.example.autotallerapp.data.device.ProveedorUbicacion
import com.example.autotallerapp.data.device.ReproductorAudio
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.usecase.BuscarClientePorDniUseCase
import com.example.autotallerapp.domain.usecase.ObservarOrdenUseCase
import com.example.autotallerapp.domain.usecase.ObtenerAutomovilesClienteUseCase
import com.example.autotallerapp.domain.usecase.RegistrarOrdenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NuevaOrdenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buscarCliente: BuscarClientePorDniUseCase,
    private val obtenerAutomoviles: ObtenerAutomovilesClienteUseCase,
    private val registrarOrden: RegistrarOrdenUseCase,
    private val observarOrden: ObservarOrdenUseCase,
    private val grabador: GrabadorAudio,
    private val reproductor: ReproductorAudio,
    private val proveedorUbicacion: ProveedorUbicacion
) : ViewModel() {

    private val clienteId: String = checkNotNull(savedStateHandle["clienteId"])
    private val automovilId: String = checkNotNull(savedStateHandle["automovilId"])

    private val _estado = MutableStateFlow(NuevaOrdenUiState())
    val estado: StateFlow<NuevaOrdenUiState> = _estado.asStateFlow()

    private var temporizador: Job? = null

    init {
        cargarDatos()
    }

    // ---------- Datos del cliente y del automovil ----------

    fun cargarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorCarga = null) }

            // ClienteRepository usa el Result de Kotlin; aqui se adapta en un solo punto
            val cliente = buscarCliente(clienteId).getOrNull()
            val automovil = when (val resultado = obtenerAutomoviles(clienteId)) {
                is Resultado.Exito -> resultado.dato.firstOrNull { it.id == automovilId }
                is Resultado.Error -> null
            }

            _estado.update {
                if (cliente == null || automovil == null) {
                    it.copy(cargando = false, errorCarga = "No se pudo cargar el cliente o el automovil")
                } else {
                    it.copy(cargando = false, cliente = cliente, automovil = automovil)
                }
            }
        }
    }

    // ---------- Falla ----------

    fun alCambiarFalla(valor: String) = _estado.update { it.copy(falla = valor) }

    // ---------- Fotos ----------

    fun agregarFoto(ruta: String) = _estado.update {
        if (it.puedeAgregarFoto && ruta !in it.fotos) it.copy(fotos = it.fotos + ruta) else it
    }

    fun quitarFoto(ruta: String) {
        File(ruta).delete()
        _estado.update { it.copy(fotos = it.fotos - ruta) }
    }

    // ---------- Nota de voz ----------

    fun iniciarGrabacion() {
        if (_estado.value.grabando) return
        reproductor.detener()
        _estado.value.audio?.let { File(it).delete() }

        try {
            grabador.iniciar(alAlcanzarLimite = { detenerGrabacion() })
        } catch (e: Exception) {
            _estado.update { it.copy(audio = null, mensaje = "No se pudo iniciar la grabacion") }
            return
        }

        _estado.update {
            it.copy(grabando = true, segundosGrabados = 0, audio = null, reproduciendo = false)
        }
        temporizador = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _estado.update { it.copy(segundosGrabados = it.segundosGrabados + 1) }
            }
        }
    }

    fun detenerGrabacion() {
        if (!_estado.value.grabando) return
        temporizador?.cancel()
        temporizador = null

        val ruta = grabador.detener()
        _estado.update {
            it.copy(
                grabando = false,
                audio = ruta,
                mensaje = if (ruta == null) "La grabacion fue demasiado corta" else it.mensaje
            )
        }
    }

    fun eliminarAudio() {
        reproductor.detener()
        _estado.value.audio?.let { File(it).delete() }
        _estado.update { it.copy(audio = null, reproduciendo = false, segundosGrabados = 0) }
    }

    fun alternarReproduccion() {
        val audio = _estado.value.audio ?: return
        if (_estado.value.reproduciendo) {
            reproductor.detener()
            _estado.update { it.copy(reproduciendo = false) }
            return
        }
        try {
            reproductor.reproducir(audio) { _estado.update { it.copy(reproduciendo = false) } }
            _estado.update { it.copy(reproduciendo = true) }
        } catch (e: Exception) {
            _estado.update { it.copy(mensaje = "No se pudo reproducir la nota de voz") }
        }
    }

    // ---------- Ubicacion ----------

    fun alCambiarUbicacion(activar: Boolean) {
        if (!activar) {
            _estado.update { it.copy(incluirUbicacion = false, ubicacion = null) }
            return
        }
        _estado.update { it.copy(incluirUbicacion = true, obteniendoUbicacion = true) }

        viewModelScope.launch {
            val ubicacion = proveedorUbicacion.obtenerActual()
            _estado.update {
                it.copy(
                    obteniendoUbicacion = false,
                    ubicacion = ubicacion,
                    incluirUbicacion = ubicacion != null,
                    mensaje = if (ubicacion == null) {
                        "No se pudo obtener la ubicacion. Revisa que el GPS este activo"
                    } else {
                        it.mensaje
                    }
                )
            }
        }
    }

    // ---------- Registro ----------

    fun registrar() {
        val actual = _estado.value
        if (actual.guardando) return
        if (actual.grabando) {
            mostrarMensaje("Deten la grabacion antes de registrar")
            return
        }
        val cliente = actual.cliente ?: return
        val automovil = actual.automovil ?: return
        reproductor.detener()

        val orden = OrdenServicio(
            clienteDni = cliente.dni,
            clienteNombre = cliente.nombreCompleto,
            automovilId = automovil.id,
            placa = automovil.placa,
            marca = automovil.marca,
            modelo = automovil.modelo,
            falla = actual.falla,
            fotos = actual.fotos,
            audio = actual.audio,
            ubicacion = if (actual.incluirUbicacion) actual.ubicacion else null
        )

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, reproduciendo = false) }
            when (val resultado = registrarOrden(orden)) {
                is Resultado.Exito -> {
                    // Los archivos ya se movieron a la carpeta de la orden
                    _estado.update {
                        it.copy(
                            guardando = false,
                            ordenRegistrada = resultado.dato,
                            fotos = emptyList(),
                            audio = null
                        )
                    }
                    seguirOrden(resultado.dato.idLocal)
                }
                is Resultado.Error ->
                    _estado.update { it.copy(guardando = false, mensaje = resultado.mensaje) }
            }
        }
    }

    /** Cuando el Worker envie la orden, Room avisa y el dialogo muestra el numero. */
    private fun seguirOrden(idLocal: String) {
        viewModelScope.launch {
            observarOrden(idLocal).filterNotNull().collect { orden ->
                _estado.update { it.copy(ordenRegistrada = orden) }
            }
        }
    }

    // ---------- Mensajes ----------

    fun mostrarMensaje(texto: String) = _estado.update { it.copy(mensaje = texto) }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }

    override fun onCleared() {
        grabador.cancelar()
        reproductor.detener()
        // Si se sale sin registrar, se borran las capturas temporales
        val actual = _estado.value
        if (actual.ordenRegistrada == null) {
            actual.fotos.forEach { File(it).delete() }
            actual.audio?.let { File(it).delete() }
        }
        super.onCleared()
    }
}

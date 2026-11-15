package com.example.autotallerapp.ui.screens.cierre

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.MensajeAvisoCliente
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.usecase.BuscarClientePorDniUseCase
import com.example.autotallerapp.domain.usecase.BuscarProblemasSimilaresUseCase
import com.example.autotallerapp.domain.usecase.CerrarOrdenUseCase
import com.example.autotallerapp.domain.usecase.ObtenerDetalleCierreUseCase
import com.example.autotallerapp.domain.usecase.RegistrarAvisoClienteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** HU08: registro del informe tecnico y cierre de la OST. */
@HiltViewModel
class InformeTecnicoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obtenerDetalleCierre: ObtenerDetalleCierreUseCase,
    private val buscarSimilares: BuscarProblemasSimilaresUseCase,
    private val cerrarOrden: CerrarOrdenUseCase,
    private val buscarClientePorDni: BuscarClientePorDniUseCase,
    private val registrarAvisoCliente: RegistrarAvisoClienteUseCase
) : ViewModel() {

    /** Viene del argumento {ordenId} de la ruta de navegacion. */
    private val idOrden: String = savedStateHandle.get<String>("ordenId").orEmpty()

    private val _estado = MutableStateFlow(InformeTecnicoUiState())
    val estado: StateFlow<InformeTecnicoUiState> = _estado.asStateFlow()

    private var busquedaSimilares: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorCarga = null) }
            when (val resultado = obtenerDetalleCierre(idOrden)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        cargando = false,
                        orden = resultado.dato.orden,
                        trabajos = resultado.dato.trabajos
                    )
                }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, errorCarga = resultado.mensaje)
                }
            }
        }
    }

    fun alCambiarSolucion(valor: String) = _estado.update { it.copy(solucion = valor) }

    fun alCambiarTrabajo(tecnicoId: String, valor: String) = _estado.update { estado ->
        estado.copy(
            trabajos = estado.trabajos.map {
                if (it.tecnicoId == tecnicoId) it.copy(trabajoRealizado = valor) else it
            }
        )
    }

    fun alCambiarRegistrarProblema(activo: Boolean) {
        _estado.update { it.copy(registrarProblema = activo) }
        if (activo) buscarCoincidencias(_estado.value.tituloProblema)
    }

    fun alSeleccionarSistema(sistema: SistemaAutomovil) = _estado.update { it.copy(sistema = sistema) }

    fun alCambiarTitulo(valor: String) {
        _estado.update { it.copy(tituloProblema = valor) }
        buscarCoincidencias(valor)
    }

    fun alCambiarCausa(valor: String) = _estado.update { it.copy(causaProblema = valor) }

    /** Mientras se escribe el titulo se muestran los problemas parecidos de la bitacora. */
    private fun buscarCoincidencias(titulo: String) {
        busquedaSimilares?.cancel()
        busquedaSimilares = viewModelScope.launch {
            delay(400)
            val similares = buscarSimilares(titulo)
            _estado.update { it.copy(similares = similares) }
        }
    }

    fun pedirConfirmacion() = _estado.update { it.copy(confirmandoCierre = true) }

    fun cancelarConfirmacion() = _estado.update { it.copy(confirmandoCierre = false) }

    fun cerrar() {
        val actual = _estado.value
        val orden = actual.orden ?: return
        if (actual.guardando) return

        val problema = if (actual.registrarProblema) {
            ProblemaTecnico(
                id = "",
                sistema = actual.sistema,
                marca = orden.marca,
                modelo = orden.modelo,
                titulo = actual.tituloProblema,
                descripcion = actual.causaProblema,
                solucion = actual.solucion,
                actualizadoEn = System.currentTimeMillis()
            )
        } else {
            null
        }

        val informe = InformeTecnico(
            idOrden = orden.idLocal,
            solucionAplicada = actual.solucion,
            trabajos = actual.trabajos,
            problemaNuevo = problema
        )

        viewModelScope.launch {
            _estado.update { it.copy(confirmandoCierre = false, guardando = true) }
            when (val resultado = cerrarOrden(informe)) {
                is Resultado.Exito -> {
                    _estado.update {
                        it.copy(guardando = false, cerrada = true, problemaRegistrado = resultado.dato)
                    }
                    prepararAviso(orden, informe.solucionAplicada)
                }
                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    // ---------- Aviso al cliente (criterio 5) ----------

    /** Busca el telefono del cliente (HU02) y arma el mensaje de "vehiculo listo". */
    private fun prepararAviso(orden: OrdenServicio, solucion: String) {
        viewModelScope.launch {
            _estado.update { it.copy(preparandoAviso = true) }
            val cliente = buscarClientePorDni(orden.clienteDni).getOrNull()
            val nombre = cliente?.nombreCompleto?.ifBlank { null } ?: orden.clienteNombre

            _estado.update {
                it.copy(
                    preparandoAviso = false,
                    nombreCliente = nombre,
                    telefonoCliente = MensajeAvisoCliente.normalizarTelefono(cliente?.telefono.orEmpty()),
                    mensajeAviso = MensajeAvisoCliente.armar(
                        nombreCliente = nombre,
                        marca = orden.marca,
                        modelo = orden.modelo,
                        placa = orden.placa,
                        numeroOrden = orden.numeroFormateado,
                        trabajoRealizado = solucion.trim()
                    )
                )
            }
        }
    }

    /** La pantalla ya abrio WhatsApp o SMS; aqui se deja constancia en la OST. */
    fun alAvisarCliente(canal: CanalAviso) {
        _estado.update { it.copy(avisoEnviadoPor = canal) }
        viewModelScope.launch {
            val resultado = registrarAvisoCliente(idOrden, canal)
            if (resultado is Resultado.Error) {
                _estado.update { it.copy(mensaje = resultado.mensaje) }
            }
        }
    }

    fun avisoNoDisponible(texto: String) = _estado.update { it.copy(mensaje = texto) }
}

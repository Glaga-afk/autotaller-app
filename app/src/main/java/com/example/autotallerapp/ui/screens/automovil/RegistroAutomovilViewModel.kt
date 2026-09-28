package com.example.autotallerapp.ui.screens.automovil

import androidx.lifecycle.SavedStateHandle
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
import com.example.autotallerapp.core.PlacaValidador
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.model.Modelo
import com.example.autotallerapp.domain.usecase.ObtenerMarcasUseCase
import com.example.autotallerapp.domain.usecase.ObtenerModelosUseCase
import com.example.autotallerapp.domain.usecase.RegistrarAutomovilUseCase
import javax.inject.Inject

@HiltViewModel
class RegistroAutomovilViewModel @Inject constructor(
    private val obtenerMarcas: ObtenerMarcasUseCase,
    private val obtenerModelos: ObtenerModelosUseCase,
    private val registrarAutomovil: RegistrarAutomovilUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val clienteId: String = checkNotNull(savedStateHandle["clienteId"])

    private val _estado = MutableStateFlow(RegistroAutomovilUiState())
    val estado: StateFlow<RegistroAutomovilUiState> = _estado.asStateFlow()

    private val _eventos = Channel<RegistroAutomovilEvento>()
    val eventos = _eventos.receiveAsFlow()

    init {
        viewModelScope.launch {
            obtenerMarcas().collect { marcas -> _estado.update { it.copy(marcas = marcas) } }
        }
    }

    fun alCambiarPlaca(valor: String) =
        _estado.update { it.copy(placa = valor, errorPlaca = null, mensajeError = null) }

    fun alSeleccionarMarca(marca: Marca) {
        _estado.update {
            it.copy(
                marcaSeleccionada = marca,
                modeloSeleccionado = null,
                modelos = emptyList(),
                errorMarca = null,
                mensajeError = null
            )
        }
        viewModelScope.launch {
            obtenerModelos(marca.id).collect { modelos -> _estado.update { it.copy(modelos = modelos) } }
        }
    }

    fun alSeleccionarModelo(modelo: Modelo) =
        _estado.update { it.copy(modeloSeleccionado = modelo, errorModelo = null, mensajeError = null) }

    fun alCambiarAnio(valor: String) {
        if (valor.length <= 4 && valor.all { it.isDigit() }) _estado.update { it.copy(anio = valor) }
    }

    fun alCambiarColor(valor: String) = _estado.update { it.copy(color = valor) }

    fun limpiarMensaje() = _estado.update { it.copy(mensajeError = null) }

    fun alPulsarRegistrar() {
        val actual = _estado.value

        val errorPlaca = if (!PlacaValidador.esValida(actual.placa)) "La placa no tiene un formato valido" else null
        val errorMarca = if (actual.marcaSeleccionada == null) "Selecciona una marca" else null
        val errorModelo = if (actual.modeloSeleccionado == null) "Selecciona un modelo" else null

        if (errorPlaca != null || errorMarca != null || errorModelo != null) {
            _estado.update { it.copy(errorPlaca = errorPlaca, errorMarca = errorMarca, errorModelo = errorModelo) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            val resultado = registrarAutomovil(
                clienteId = clienteId,
                placa = actual.placa,
                marca = actual.marcaSeleccionada!!.nombre,
                modelo = actual.modeloSeleccionado!!.nombre,
                anio = actual.anio.toIntOrNull(),
                color = actual.color
            )
            when (resultado) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false) }
                    _eventos.send(RegistroAutomovilEvento.AutomovilRegistrado)
                }
                is Resultado.Error ->
                    _estado.update { it.copy(cargando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}
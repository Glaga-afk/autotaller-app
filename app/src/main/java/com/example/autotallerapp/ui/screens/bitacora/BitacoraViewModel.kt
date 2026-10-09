package com.example.autotallerapp.ui.screens.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.usecase.BuscarProblemasUseCase
import com.example.autotallerapp.domain.usecase.HayBitacoraLocalUseCase
import com.example.autotallerapp.domain.usecase.SincronizarBitacoraUseCase
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val buscarProblemas: BuscarProblemasUseCase,
    private val sincronizarBitacora: SincronizarBitacoraUseCase,
    private val hayBitacoraLocal: HayBitacoraLocalUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(BitacoraUiState())
    val estado: StateFlow<BitacoraUiState> = _estado.asStateFlow()

    private val _filtro = MutableStateFlow(FiltroBitacora())

    init {
        viewModelScope.launch {
            _estado.update { it.copy(bitacoraVacia = !hayBitacoraLocal()) }
        }

        viewModelScope.launch {
            _filtro
                .debounce(300)
                .flatMapLatest { filtro -> buscarProblemas(filtro) }
                .collect { resultados ->
                    _estado.update { it.copy(resultados = resultados, cargando = false) }
                }
        }
    }

    fun alCambiarPalabraClave(valor: String) {
        _estado.update { it.copy(palabraClave = valor, cargando = true) }
        _filtro.update { it.copy(palabraClave = valor) }
    }

    fun alSeleccionarSistema(sistema: SistemaAutomovil?) {
        _estado.update { it.copy(sistemaSeleccionado = sistema, cargando = true) }
        _filtro.update { it.copy(sistema = sistema) }
    }

    fun alCambiarMarca(valor: String) {
        _estado.update { it.copy(marca = valor, cargando = true) }
        _filtro.update { it.copy(marca = valor) }
    }

    fun alCambiarModelo(valor: String) {
        _estado.update { it.copy(modelo = valor, cargando = true) }
        _filtro.update { it.copy(modelo = valor) }
    }

    fun alAlternarFiltros() = _estado.update { it.copy(filtrosVisibles = !it.filtrosVisibles) }

    fun limpiarFiltros() {
        _estado.update {
            it.copy(palabraClave = "", sistemaSeleccionado = null, marca = "", modelo = "", cargando = true)
        }
        _filtro.value = FiltroBitacora()
    }

    fun alPulsarSincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true) }
            when (val resultado = sincronizarBitacora()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        sincronizando = false,
                        bitacoraVacia = false,
                        mensaje = "Bitacora actualizada: ${resultado.dato} casos"
                    )
                }
                is Resultado.Error -> _estado.update {
                    it.copy(sincronizando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }
}
package com.example.autotallerapp.ui.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.usecase.BuscarClientePorDniUseCase
import com.example.autotallerapp.domain.usecase.RegistrarClienteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val buscarClientePorDniUseCase: BuscarClientePorDniUseCase,
    private val registrarClienteUseCase: RegistrarClienteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClienteUiState>(ClienteUiState.Inicial)
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    fun buscarPorDni(dni: String) {
        viewModelScope.launch {
            _uiState.value = ClienteUiState.Cargando
            val result = buscarClientePorDniUseCase(dni)
            result.onSuccess { cliente ->
                if (cliente != null) {
                    _uiState.value = ClienteUiState.ClienteEncontrado(cliente)
                } else {
                    _uiState.value = ClienteUiState.ClienteNoEncontrado(dni)
                }
            }.onFailure { error ->
                _uiState.value = ClienteUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }

    fun registrarCliente(dni: String, nombre: String, direccion: String, sexo: String, telefono: String) {
        viewModelScope.launch {
            _uiState.value = ClienteUiState.Cargando
            val nuevoCliente = Cliente(dni.trim(), nombre.trim(), direccion.trim(), sexo, telefono.trim())
            val result = registrarClienteUseCase(nuevoCliente)
            result.onSuccess {
                _uiState.value = ClienteUiState.RegistroExitoso(
                    nuevoCliente.dni
                )
            }.onFailure { error ->
                _uiState.value = ClienteUiState.Error(
                    error.message ?: "Error al registrar"
                )
            }
        }
    }

    fun limpiarEstado() {
        _uiState.value = ClienteUiState.Inicial
    }
}
package com.example.autotallerapp.ui.cliente

import com.example.autotallerapp.domain.model.Cliente

sealed interface ClienteUiState {
    object Inicial : ClienteUiState
    object Cargando : ClienteUiState
    data class ClienteEncontrado(val cliente: Cliente) : ClienteUiState
    data class ClienteNoEncontrado(val dniBuscado: String) : ClienteUiState
    object RegistroExitoso : ClienteUiState
    data class Error(val mensaje: String) : ClienteUiState
}
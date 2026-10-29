package com.example.autotallerapp.ui.cliente

import com.example.autotallerapp.domain.model.Cliente

sealed interface ClienteUiState {
    object Inicial : ClienteUiState
    object Cargando : ClienteUiState
    data class Exito(val clientes: List<Cliente>) : ClienteUiState // <-- Nuevo estado para el directorio
    data class ClienteEncontrado(val cliente: Cliente) : ClienteUiState
    data class ClienteNoEncontrado(val dniBuscado: String) : ClienteUiState
    data class RegistroExitoso(
        val dni: String
    ) : ClienteUiState
    data class Error(val mensaje: String) : ClienteUiState
}
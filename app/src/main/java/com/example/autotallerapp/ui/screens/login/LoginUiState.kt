package com.example.autotallerapp.ui.screens.login

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false,
    val cargandoGoogle: Boolean = false
) {
    val formularioHabilitado: Boolean get() = !cargando && !cargandoGoogle
}

sealed interface LoginEvento {
    data object IrAInicio : LoginEvento
    data object IrAPendiente : LoginEvento
}

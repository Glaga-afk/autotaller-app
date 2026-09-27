package com.example.autotallerapp.domain.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.PENDIENTE,
    val activo: Boolean = true
)

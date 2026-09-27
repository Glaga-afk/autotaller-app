package com.example.autotallerapp.data.remote.dto

import com.example.autotallerapp.domain.model.Rol
import com.example.autotallerapp.domain.model.Usuario

data class UsuarioDto(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = Rol.PENDIENTE.name,
    val activo: Boolean = true
) {
    fun aDominio(): Usuario = Usuario(
        uid = uid,
        nombre = nombre,
        correo = correo,
        rol = Rol.desde(rol),
        activo = activo
    )

    companion object {
        fun desdeDominio(usuario: Usuario): UsuarioDto = UsuarioDto(
            uid = usuario.uid,
            nombre = usuario.nombre,
            correo = usuario.correo,
            rol = usuario.rol.name,
            activo = usuario.activo
        )
    }
}

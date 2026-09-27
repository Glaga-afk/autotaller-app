package com.example.autotallerapp.ui.screens.inicio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.autotallerapp.domain.model.Rol
import com.example.autotallerapp.ui.navigation.Rutas

data class OpcionMenu(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String
)

object MenuPorRol {

    fun opciones(rol: Rol): List<OpcionMenu> = when (rol) {
        Rol.RECEPCIONISTA -> listOf(
            OpcionMenu("Clientes", Icons.Outlined.People, Rutas.CLIENTES),
            OpcionMenu("Automoviles", Icons.Outlined.DirectionsCar, Rutas.AUTOMOVILES),
            OpcionMenu("Nueva OST", Icons.Outlined.NoteAdd, Rutas.NUEVA_ORDEN),
            OpcionMenu("Ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES)
        )
        Rol.TECNICO_SUPERVISOR -> listOf(
            OpcionMenu("Ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES),
            OpcionMenu("Bitacora", Icons.Outlined.MenuBook, Rutas.BITACORA),
            OpcionMenu("Tecnicos", Icons.Outlined.Groups, Rutas.TECNICOS),
            OpcionMenu("Diagnostico IA", Icons.Outlined.AutoAwesome, Rutas.DIAGNOSTICO_IA)
        )
        Rol.TECNICO -> listOf(
            OpcionMenu("Mis ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES),
            OpcionMenu("Bitacora", Icons.Outlined.MenuBook, Rutas.BITACORA),
            OpcionMenu("Asistente IA", Icons.Outlined.Chat, Rutas.ASISTENTE_IA),
            OpcionMenu("Mi perfil", Icons.Outlined.Person, Rutas.PERFIL)
        )
        Rol.ADMINISTRADOR -> listOf(
            OpcionMenu("Usuarios y roles", Icons.Outlined.VerifiedUser, Rutas.USUARIOS),
            OpcionMenu("Indicadores", Icons.Outlined.PieChart, Rutas.INDICADORES),
            OpcionMenu("Catalogos", Icons.Outlined.Category, Rutas.CATALOGOS),
            OpcionMenu("Ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES)
        )
        Rol.PENDIENTE -> emptyList()
    }

    fun opcionesBarraInferior(rol: Rol): List<OpcionMenu> {
        val inicio = OpcionMenu("Inicio", Icons.Outlined.Build, Rutas.INICIO)
        return when (rol) {
            Rol.RECEPCIONISTA -> listOf(
                inicio,
                OpcionMenu("OST", Icons.Outlined.NoteAdd, Rutas.NUEVA_ORDEN),
                OpcionMenu("Clientes", Icons.Outlined.People, Rutas.CLIENTES)
            )
            Rol.TECNICO_SUPERVISOR -> listOf(
                inicio,
                OpcionMenu("Ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES),
                OpcionMenu("Bitacora", Icons.Outlined.MenuBook, Rutas.BITACORA)
            )
            Rol.TECNICO -> listOf(
                inicio,
                OpcionMenu("Ordenes", Icons.AutoMirrored.Outlined.ListAlt, Rutas.ORDENES),
                OpcionMenu("Asistente", Icons.Outlined.Chat, Rutas.ASISTENTE_IA)
            )
            Rol.ADMINISTRADOR -> listOf(
                inicio,
                OpcionMenu("Usuarios", Icons.Outlined.VerifiedUser, Rutas.USUARIOS),
                OpcionMenu("Indicadores", Icons.Outlined.PieChart, Rutas.INDICADORES)
            )
            Rol.PENDIENTE -> listOf(inicio)
        }
    }
}

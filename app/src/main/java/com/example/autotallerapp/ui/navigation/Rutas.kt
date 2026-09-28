package com.example.autotallerapp.ui.navigation

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val INICIO = "inicio"
    const val PENDIENTE = "pendiente"

    const val CLIENTES = "clientes"
    const val AUTOMOVILES = "automoviles"
    const val ORDENES = "ordenes"
    const val NUEVA_ORDEN = "nueva_orden"
    const val BITACORA = "bitacora"
    const val TECNICOS = "tecnicos"
    const val DIAGNOSTICO_IA = "diagnostico_ia"
    const val ASISTENTE_IA = "asistente_ia"
    const val USUARIOS = "usuarios"
    const val INDICADORES = "indicadores"
    const val CATALOGOS = "catalogos"
    const val PERFIL = "perfil"

    //Automovil
    const val NUEVO_AUTOMOVIL = "nuevo_automovil/{clienteId}"
    const val ESCANER_PLACA = "escaner_placa"

    fun rutaNuevoAutomovil(clienteId: String) = "nuevo_automovil/$clienteId"

    //Automovil-Cliente
    const val AUTOMOVILES_CLIENTE = "automoviles_cliente/{clienteId}"

    fun rutaAutomovilesCliente(clienteId: String) = "automoviles_cliente/$clienteId"
}

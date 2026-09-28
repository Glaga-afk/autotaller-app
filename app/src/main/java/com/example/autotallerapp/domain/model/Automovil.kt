package com.example.autotallerapp.domain.model

data class Automovil(
    val id: String = "",
    val clienteId: String = "",
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val anio: Int? = null,
    val color: String = "",
    val creadoEn: Long = System.currentTimeMillis()
)

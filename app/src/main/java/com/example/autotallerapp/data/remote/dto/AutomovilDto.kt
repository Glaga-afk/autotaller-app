package com.example.autotallerapp.data.remote.dto

import com.example.autotallerapp.domain.model.Automovil

data class AutomovilDto(
    val id: String = "",
    val clienteId: String = "",
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val anio: Int? = null,
    val color: String = "",
    val creadoEn: Long = 0
) {
    fun aDominio(): Automovil = Automovil(
        id = id, clienteId = clienteId, placa = placa, marca = marca,
        modelo = modelo, anio = anio, color = color, creadoEn = creadoEn
    )

    companion object {
        fun desdeDominio(automovil: Automovil): AutomovilDto = AutomovilDto(
            id = automovil.id, clienteId = automovil.clienteId, placa = automovil.placa,
            marca = automovil.marca, modelo = automovil.modelo, anio = automovil.anio,
            color = automovil.color, creadoEn = automovil.creadoEn
        )
    }
}

package com.example.autotallerapp.data.remote.dto

import com.example.autotallerapp.data.local.entity.ProblemaEntity
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil

data class ProblemaTecnicoDto(
    val id: String = "",
    val sistema: String = "",
    val marca: String = "",
    val modelo: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val solucion: String = "",
    val actualizadoEn: Long = 0
) {
    fun aDominio(): ProblemaTecnico = ProblemaTecnico(
        id = id,
        sistema = SistemaAutomovil.desde(sistema),
        marca = marca,
        modelo = modelo,
        titulo = titulo,
        descripcion = descripcion,
        solucion = solucion,
        actualizadoEn = actualizadoEn
    )

    fun aEntity(): ProblemaEntity = ProblemaEntity(
        id = id, sistema = sistema, marca = marca, modelo = modelo,
        titulo = titulo, descripcion = descripcion, solucion = solucion, actualizadoEn = actualizadoEn
    )
}

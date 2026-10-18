package com.example.autotallerapp.data.remote.dto

import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Documento de la coleccion "ordenes" en Firestore.
 *
 * Copia el nombre del cliente y los datos del automovil dentro de la orden:
 * asi la bandeja de ordenes (HU07) puede buscar por placa o DNI y mostrar
 * la lista sin hacer una consulta extra por cada orden.
 */
data class OrdenDto(
    val id: String = "",
    val numero: Long = 0,
    val estado: String = EstadoOrden.PENDIENTE.name,
    val clienteDni: String = "",
    val clienteNombre: String = "",
    val automovilId: String = "",
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val falla: String = "",
    val fotos: List<String> = emptyList(),
    val audio: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaRegistro: Timestamp = Timestamp.now(),
    val registradoPor: String = "",
    val sincronizadoEn: Timestamp = Timestamp.now()
) {
    companion object {
        fun desdeDominio(
            orden: OrdenServicio,
            numero: Long,
            fotosRemotas: List<String>,
            audioRemoto: String?
        ): OrdenDto = OrdenDto(
            id = orden.idLocal,
            numero = numero,
            estado = orden.estado.name,
            clienteDni = orden.clienteDni,
            clienteNombre = orden.clienteNombre,
            automovilId = orden.automovilId,
            placa = orden.placa,
            marca = orden.marca,
            modelo = orden.modelo,
            falla = orden.falla,
            fotos = fotosRemotas,
            audio = audioRemoto,
            latitud = orden.ubicacion?.latitud,
            longitud = orden.ubicacion?.longitud,
            // La fecha es la del registro en el celular, no la del envio
            fechaRegistro = Timestamp(Date(orden.fechaRegistro)),
            registradoPor = orden.registradoPor,
            sincronizadoEn = Timestamp.now()
        )
    }
}

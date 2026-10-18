package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autotallerapp.domain.model.EstadoEnvio
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.Ubicacion

/** Copia local de la orden. Permite registrarla sin internet y enviarla despues. */
@Entity(tableName = "ordenes")
data class OrdenEntity(
    @PrimaryKey val idLocal: String,
    val numero: Long?,
    val estado: String,
    val estadoEnvio: String,
    val clienteDni: String,
    val clienteNombre: String,
    val automovilId: String,
    val placa: String,
    val marca: String,
    val modelo: String,
    val falla: String,
    val fotos: List<String>,
    val audio: String?,
    val latitud: Double?,
    val longitud: Double?,
    val fechaRegistro: Long,
    val registradoPor: String,
    val intentos: Int = 0,
    val ultimoError: String? = null
) {
    fun aDominio(): OrdenServicio = OrdenServicio(
        idLocal = idLocal,
        numero = numero,
        estado = EstadoOrden.desde(estado),
        estadoEnvio = EstadoEnvio.desde(estadoEnvio),
        clienteDni = clienteDni,
        clienteNombre = clienteNombre,
        automovilId = automovilId,
        placa = placa,
        marca = marca,
        modelo = modelo,
        falla = falla,
        fotos = fotos,
        audio = audio,
        ubicacion = if (latitud != null && longitud != null) Ubicacion(latitud, longitud) else null,
        fechaRegistro = fechaRegistro,
        registradoPor = registradoPor
    )

    companion object {
        fun desdeDominio(orden: OrdenServicio): OrdenEntity = OrdenEntity(
            idLocal = orden.idLocal,
            numero = orden.numero,
            estado = orden.estado.name,
            estadoEnvio = orden.estadoEnvio.name,
            clienteDni = orden.clienteDni,
            clienteNombre = orden.clienteNombre,
            automovilId = orden.automovilId,
            placa = orden.placa,
            marca = orden.marca,
            modelo = orden.modelo,
            falla = orden.falla,
            fotos = orden.fotos,
            audio = orden.audio,
            latitud = orden.ubicacion?.latitud,
            longitud = orden.ubicacion?.longitud,
            fechaRegistro = orden.fechaRegistro,
            registradoPor = orden.registradoPor
        )
    }
}

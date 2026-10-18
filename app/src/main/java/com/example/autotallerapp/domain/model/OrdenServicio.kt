package com.example.autotallerapp.domain.model

import java.util.Locale

/**
 * Orden de Servicio Tecnico (OST).
 *
 * - idLocal: identificador unico generado en el celular. Tambien es el id del
 *   documento en Firestore, asi un reintento de envio nunca duplica la orden.
 * - numero: correlativo del taller. Es null mientras la orden no se envie,
 *   porque solo puede asignarse con conexion (transaccion en Firestore).
 * - fotos y audio: rutas de los archivos guardados en el celular.
 */
data class OrdenServicio(
    val idLocal: String = "",
    val numero: Long? = null,
    val estado: EstadoOrden = EstadoOrden.PENDIENTE,
    val estadoEnvio: EstadoEnvio = EstadoEnvio.PENDIENTE_ENVIO,
    val clienteDni: String = "",
    val clienteNombre: String = "",
    val automovilId: String = "",
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val falla: String = "",
    val fotos: List<String> = emptyList(),
    val audio: String? = null,
    val ubicacion: Ubicacion? = null,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val registradoPor: String = ""
) {
    /** Numero con formato de taller, por ejemplo OST-000027. */
    val numeroFormateado: String?
        get() = numero?.let { String.format(Locale.ROOT, "OST-%06d", it) }
}

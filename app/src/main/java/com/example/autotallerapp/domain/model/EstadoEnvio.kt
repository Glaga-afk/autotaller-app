package com.example.autotallerapp.domain.model

/**
 * Indica si una orden registrada en el celular ya llego a Firestore.
 * Es independiente de EstadoOrden: una orden puede estar PENDIENTE de atencion
 * y a la vez PENDIENTE_ENVIO porque se registro sin conexion.
 */
enum class EstadoEnvio {
    PENDIENTE_ENVIO,
    ENVIADA,
    ERROR;

    companion object {
        fun desde(valor: String?): EstadoEnvio =
            entries.firstOrNull { it.name == valor } ?: PENDIENTE_ENVIO
    }
}

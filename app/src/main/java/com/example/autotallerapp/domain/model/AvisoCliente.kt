package com.example.autotallerapp.domain.model

/** Canal por el que se aviso al cliente que su vehiculo esta listo (HU08). */
enum class CanalAviso(val etiqueta: String) {
    WHATSAPP("WhatsApp"),
    SMS("SMS")
}

/**
 * Arma el aviso para el cliente (HU08, criterio 5).
 *
 * En el plan Spark no hay servidor para enviar push FCM y el cliente no usa la app,
 * por eso el aviso se abre en WhatsApp (o SMS) con el texto listo y el supervisor lo envia.
 */
object MensajeAvisoCliente {

    private const val CODIGO_PERU = "51"
    private const val MAXIMO_TRABAJO = 300

    /**
     * Deja el telefono en formato internacional sin "+" (ej. 51987654321), que es
     * el que piden los enlaces de WhatsApp. Devuelve null si no es un celular peruano.
     */
    fun normalizarTelefono(telefono: String): String? {
        val digitos = telefono.filter { it.isDigit() }
        return when {
            digitos.length == 9 && digitos.startsWith("9") -> CODIGO_PERU + digitos
            digitos.length == 11 && digitos.startsWith(CODIGO_PERU + "9") -> digitos
            else -> null
        }
    }

    fun armar(
        nombreCliente: String,
        marca: String,
        modelo: String,
        placa: String,
        numeroOrden: String?,
        trabajoRealizado: String
    ): String {
        val saludo = nombreCliente.trim().ifBlank { "estimado cliente" }
        val orden = numeroOrden?.let { " (orden $it)" }.orEmpty()
        val trabajo = trabajoRealizado.trim().let {
            if (it.length > MAXIMO_TRABAJO) it.take(MAXIMO_TRABAJO).trimEnd() + "..." else it
        }

        return buildString {
            append("Hola $saludo, le saludamos de AutoTaller.\n\n")
            append("Su vehículo ${marca.trim()} ${modelo.trim()} de placa ${placa.trim()}$orden ")
            append("ya está listo para ser recogido.")
            if (trabajo.isNotEmpty()) append("\n\nTrabajo realizado: $trabajo")
            append("\n\n¡Gracias por su confianza!")
        }
    }
}

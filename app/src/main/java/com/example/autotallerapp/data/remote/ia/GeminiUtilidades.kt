package com.example.autotallerapp.data.remote.ia

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

object GeminiUtilidades {
    private const val MAX_INTENTOS = 2
    private const val ESPERA_MS = 1_500L
    private val CODIGOS_PASAJEROS = setOf(500, 502, 503, 504)

    /** Reintenta solo errores pasajeros (servidor ocupado o tiempo agotado). */
    suspend fun <T> conReintento(bloque: suspend () -> T): T {
        var intento = 1
        while (true) {
            try {
                return bloque()
            } catch (e: Exception) {
                if (e is CancellationException || intento >= MAX_INTENTOS || !esPasajero(e)) throw e
                delay(ESPERA_MS * intento)
                intento++
            }
        }
    }

    fun traducirError(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            400, 401, 403 -> "Gemini rechazo la solicitud. Revisa la clave GEMINI_API_KEY"
            404 -> "El modelo de Gemini no existe o ya no esta disponible. Revisa GEMINI_MODEL"
            429 -> "Se alcanzo el limite de uso de Gemini. Espera un momento y reintenta"
            in 500..599 -> "El servicio de Gemini esta ocupado. Reintenta en unos segundos"
            else -> "Gemini respondio con un error (${e.code()}). Reintenta"
        }
        is SocketTimeoutException -> "La IA tardo demasiado en responder. Reintenta"
        is IOException -> "Sin conexion. Revisa tu internet e intentalo de nuevo"
        else -> "No se pudo obtener la respuesta. Intentalo de nuevo"
    }

    private fun esPasajero(e: Exception): Boolean =
        e is SocketTimeoutException || (e is HttpException && e.code() in CODIGOS_PASAJEROS)
}
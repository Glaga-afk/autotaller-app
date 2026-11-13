package com.example.autotallerapp.data.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Voz a texto con SpeechRecognizer. Hay que usarlo desde el hilo principal.
 * Entrega resultados parciales mientras el tecnico habla y uno final al terminar.
 */
class ReconocedorVoz @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private var reconocedor: SpeechRecognizer? = null
    private val manejador = Handler(Looper.getMainLooper())

    fun estaDisponible(): Boolean = SpeechRecognizer.isRecognitionAvailable(contexto)

    /**
     * Empieza a escuchar. [alError] recibe el mensaje a mostrar, o null si la escucha
     * termino sin nada que avisar (por ejemplo, al cancelarla).
     */
    fun iniciar(
        alParcial: (String) -> Unit,
        alFinal: (String) -> Unit,
        alError: (String?) -> Unit
    ) {
        cancelar()
        val nuevo = SpeechRecognizer.createSpeechRecognizer(contexto)

        nuevo.setRecognitionListener(object : RecognitionListener {
            override fun onResults(resultados: Bundle?) {
                val texto = primerResultado(resultados)
                liberarLuego(nuevo)
                if (texto.isBlank()) alError(MENSAJE_NO_ENTENDIO) else alFinal(texto)
            }

            override fun onPartialResults(parciales: Bundle?) {
                val texto = primerResultado(parciales)
                if (texto.isNotBlank()) alParcial(texto)
            }

            override fun onError(codigo: Int) {
                liberarLuego(nuevo)
                // ERROR_CLIENT aparece al cancelar a proposito: no es un fallo
                alError(if (codigo == SpeechRecognizer.ERROR_CLIENT) null else traducir(codigo))
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        reconocedor = nuevo
        nuevo.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, IDIOMA)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
        )
    }

    /** Deja de escuchar y procesa lo que ya se grabo: el resultado llega por alFinal. */
    fun detener() {
        reconocedor?.stopListening()
    }

    /** Descarta la escucha en curso. */
    fun cancelar() {
        reconocedor?.let {
            it.cancel()
            it.destroy()
        }
        reconocedor = null
    }

    // destroy() dentro del propio callback puede fallar: se hace en el siguiente ciclo
    private fun liberarLuego(objetivo: SpeechRecognizer) {
        manejador.post {
            objetivo.destroy()
            if (reconocedor === objetivo) reconocedor = null
        }
    }

    private fun primerResultado(datos: Bundle?): String =
        datos?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()

    private fun traducir(codigo: Int): String = when (codigo) {
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> MENSAJE_NO_ENTENDIO
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Sin conexion para reconocer la voz"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta el permiso del microfono"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor de voz esta ocupado. Intentalo de nuevo"
        SpeechRecognizer.ERROR_AUDIO -> "No se pudo usar el microfono"
        else -> "No se pudo reconocer la voz. Intentalo de nuevo"
    }

    private companion object {
        const val IDIOMA = "es-PE"
        const val MENSAJE_NO_ENTENDIO = "No te escuche. Intentalo de nuevo"
    }
}
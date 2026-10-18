package com.example.autotallerapp.data.device

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.autotallerapp.data.local.GestorArchivosOrden
import com.example.autotallerapp.domain.model.TipoArchivo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Graba la nota de voz de la falla con MediaRecorder.
 * AAC a 32 kbps: un minuto ocupa unos 240 KB, suficiente para la voz
 * y dentro del limite de Firestore.
 */
class GrabadorAudio @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private var grabador: MediaRecorder? = null
    private var archivo: File? = null
    private var limiteAlcanzado = false

    /** Empieza a grabar. Llama a [alAlcanzarLimite] si se llega a los 60 segundos. */
    fun iniciar(alAlcanzarLimite: () -> Unit) {
        cancelar()
        val destino = GestorArchivosOrden.crearArchivoCaptura(
            contexto, "nota_voz", TipoArchivo.AUDIO.extension
        )
        limiteAlcanzado = false

        val nuevo = crearMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(TASA_BITS)
            setAudioSamplingRate(FRECUENCIA_MUESTREO)
            setMaxDuration(DURACION_MAXIMA_MS)
            setOutputFile(destino.absolutePath)
            setOnInfoListener { _, codigo, _ ->
                if (codigo == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    limiteAlcanzado = true
                    // Se avisa fuera del callback para poder liberar el grabador sin riesgo
                    Handler(Looper.getMainLooper()).post { alAlcanzarLimite() }
                }
            }
            prepare()
            start()
        }
        grabador = nuevo
        archivo = destino
    }

    /** Termina la grabacion y devuelve la ruta del audio, o null si fue demasiado corta. */
    fun detener(): String? {
        val actual = grabador ?: return null
        val destino = archivo
        grabador = null
        archivo = null

        return try {
            // Si se llego al limite, MediaRecorder ya se detuvo solo
            if (!limiteAlcanzado) actual.stop()
            destino?.absolutePath
        } catch (e: RuntimeException) {
            // stop() falla si se grabo menos de un segundo: el archivo no sirve
            destino?.delete()
            null
        } finally {
            actual.release()
        }
    }

    /** Descarta la grabacion en curso. */
    fun cancelar() {
        detener()?.let { File(it).delete() }
    }

    @Suppress("DEPRECATION")
    private fun crearMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(contexto) else MediaRecorder()

    companion object {
        const val DURACION_MAXIMA_MS = 60_000
        private const val TASA_BITS = 32_000
        private const val FRECUENCIA_MUESTREO = 22_050
    }
}

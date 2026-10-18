package com.example.autotallerapp.data.device

import android.media.MediaPlayer
import javax.inject.Inject

/** Reproduce la nota de voz para que la recepcionista la revise antes de registrar. */
class ReproductorAudio @Inject constructor() {

    private var reproductor: MediaPlayer? = null

    fun reproducir(ruta: String, alTerminar: () -> Unit) {
        detener()
        reproductor = MediaPlayer().apply {
            setDataSource(ruta)
            setOnCompletionListener {
                detener()
                alTerminar()
            }
            prepare()
            start()
        }
    }

    fun detener() {
        reproductor?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        reproductor = null
    }
}

package com.example.autotallerapp.data.device

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.File
import java.io.FileOutputStream

/**
 * Prepara la foto antes de guardarla:
 * - la gira segun como se sostuvo el celular,
 * - la reduce a 1280 px en su lado mayor,
 * - la comprime en JPEG al 70%.
 * Una foto queda entre 150 y 300 KB, dentro del limite de Firestore.
 */
object ProcesadorFoto {

    private const val LADO_MAXIMO = 1280
    private const val CALIDAD_JPEG = 70

    fun guardar(original: Bitmap, rotacionGrados: Int, destino: File): File {
        val ladoMayor = maxOf(original.width, original.height)
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / ladoMayor)

        val transformacion = Matrix().apply {
            postRotate(rotacionGrados.toFloat())
            postScale(escala, escala)
        }
        val procesada = Bitmap.createBitmap(
            original, 0, 0, original.width, original.height, transformacion, true
        )

        FileOutputStream(destino).use { salida ->
            procesada.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        }
        if (procesada !== original) procesada.recycle()
        return destino
    }
}

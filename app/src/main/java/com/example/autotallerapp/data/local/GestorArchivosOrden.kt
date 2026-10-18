package com.example.autotallerapp.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maneja los archivos de evidencia en el celular.
 *
 * Las capturas (fotos y notas de voz) nacen en la cache. Al registrar la orden
 * se mueven a una carpeta propia en filesDir, que Android no borra solo,
 * para que sigan ahi hasta que el Worker las envie.
 */
@Singleton
class GestorArchivosOrden @Inject constructor(
    @ApplicationContext private val contexto: Context
) {

    suspend fun guardarEvidencias(
        idLocal: String,
        fotos: List<String>,
        audio: String?
    ): Pair<List<String>, String?> = withContext(Dispatchers.IO) {
        val carpeta = File(contexto.filesDir, "$CARPETA_ORDENES/$idLocal").apply { mkdirs() }

        val fotosGuardadas = fotos.mapIndexed { indice, ruta ->
            mover(File(ruta), File(carpeta, "foto_${indice + 1}.jpg")).absolutePath
        }
        val audioGuardado = audio?.let {
            mover(File(it), File(carpeta, "nota_voz.m4a")).absolutePath
        }
        fotosGuardadas to audioGuardado
    }

    private fun mover(origen: File, destino: File): File {
        check(origen.exists()) { "No se encontro el archivo ${origen.name}" }
        origen.copyTo(destino, overwrite = true)
        origen.delete()
        return destino
    }

    companion object {
        private const val CARPETA_ORDENES = "ordenes"
        private const val CARPETA_CAPTURAS = "capturas"

        /** Archivo temporal para una foto o nota de voz recien capturada. */
        fun crearArchivoCaptura(contexto: Context, prefijo: String, extension: String): File {
            val carpeta = File(contexto.cacheDir, CARPETA_CAPTURAS).apply { mkdirs() }
            return File(carpeta, "${prefijo}_${System.currentTimeMillis()}.$extension")
        }
    }
}

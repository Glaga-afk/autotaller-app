package com.example.autotallerapp.data.remote.almacenamiento

import android.util.Base64
import com.example.autotallerapp.domain.model.TipoArchivo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plan Spark: guarda cada archivo como texto Base64 en la subcoleccion
 * ordenes/{id}/evidencias, un documento por archivo.
 *
 * Firestore limita cada documento a 1 MiB y Base64 aumenta el tamano un 33%,
 * por eso las fotos se comprimen al capturarlas y se rechaza todo archivo
 * de mas de 700 KB.
 */
@Singleton
class AlmacenamientoFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlmacenamientoArchivos {

    override suspend fun subir(
        ordenId: String,
        rutaLocal: String,
        tipo: TipoArchivo,
        indice: Int
    ): String {
        val archivo = File(rutaLocal)
        val bytes = withContext(Dispatchers.IO) { archivo.readBytes() }
        require(bytes.size <= TAMANO_MAXIMO_BYTES) {
            "El archivo ${archivo.name} supera el tamano permitido"
        }

        // Id fijo por archivo: si el Worker reintenta, sobrescribe en lugar de duplicar
        val idEvidencia = "${tipo.name.lowercase()}_$indice"
        val referencia = firestore.collection(COLECCION_ORDENES)
            .document(ordenId)
            .collection(SUBCOLECCION_EVIDENCIAS)
            .document(idEvidencia)

        referencia.set(
            mapOf(
                "tipo" to tipo.name,
                "mime" to tipo.mime,
                "nombre" to archivo.name,
                "tamanoBytes" to bytes.size,
                "contenidoBase64" to Base64.encodeToString(bytes, Base64.NO_WRAP)
            )
        ).await()

        return "$ESQUEMA$COLECCION_ORDENES/$ordenId/$SUBCOLECCION_EVIDENCIAS/$idEvidencia"
    }

    private companion object {
        const val COLECCION_ORDENES = "ordenes"
        const val SUBCOLECCION_EVIDENCIAS = "evidencias"
        const val ESQUEMA = "firestore://"
        const val TAMANO_MAXIMO_BYTES = 700_000
    }
}

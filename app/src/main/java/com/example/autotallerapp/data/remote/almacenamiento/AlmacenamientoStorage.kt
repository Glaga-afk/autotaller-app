package com.example.autotallerapp.data.remote.almacenamiento

import android.net.Uri
import com.example.autotallerapp.domain.model.TipoArchivo
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plan Blaze: sube el archivo a Firebase Storage y devuelve su URL de descarga.
 * Hoy no se usa; queda lista para cuando el proyecto pase a Blaze.
 */
@Singleton
class AlmacenamientoStorage @Inject constructor(
    private val storage: FirebaseStorage
) : AlmacenamientoArchivos {

    override suspend fun subir(
        ordenId: String,
        rutaLocal: String,
        tipo: TipoArchivo,
        indice: Int
    ): String {
        val referencia = storage.reference
            .child("ordenes/$ordenId/${tipo.name.lowercase()}_$indice.${tipo.extension}")

        referencia.putFile(Uri.fromFile(File(rutaLocal))).await()
        return referencia.downloadUrl.await().toString()
    }
}

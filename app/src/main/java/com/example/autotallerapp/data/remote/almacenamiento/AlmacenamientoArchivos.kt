package com.example.autotallerapp.data.remote.almacenamiento

import com.example.autotallerapp.domain.model.TipoArchivo

/**
 * Donde se guardan las fotos y notas de voz de una orden.
 *
 * Hay dos implementaciones:
 * - AlmacenamientoFirestore: plan Spark (gratuito), guarda el archivo en Base64.
 * - AlmacenamientoStorage: plan Blaze, usa Firebase Storage.
 *
 * Para cambiar de una a otra solo se edita una linea en RepositoryModule.
 */
interface AlmacenamientoArchivos {

    /**
     * Sube un archivo y devuelve la referencia que se guarda en la orden.
     * Lanza una excepcion si no se pudo subir.
     */
    suspend fun subir(ordenId: String, rutaLocal: String, tipo: TipoArchivo, indice: Int): String
}

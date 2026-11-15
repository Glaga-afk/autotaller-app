package com.example.autotallerapp.data.remote.dto

import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.google.firebase.Timestamp

/** Trabajo de un tecnico dentro del informe (Firestore). */
data class TrabajoTecnicoDto(
    val tecnicoId: String = "",
    val nombreTecnico: String = "",
    val tareaAsignada: String = "",
    val trabajoRealizado: String = ""
) {
    fun aDominio(): TrabajoTecnico =
        TrabajoTecnico(tecnicoId, nombreTecnico, tareaAsignada, trabajoRealizado)
}

/**
 * Documento ordenes/{idOrden}/informe/final en Firestore (HU08).
 * Se guarda en la misma transaccion que cambia la OST a ATENDIDA.
 */
data class InformeDto(
    val idOrden: String = "",
    val solucionAplicada: String = "",
    val trabajos: List<TrabajoTecnicoDto> = emptyList(),
    val problemaNuevoId: String? = null,
    val fechaCierre: Timestamp = Timestamp.now(),
    val cerradoPor: String = ""
) {
    companion object {
        fun desdeDominio(
            informe: InformeTecnico,
            problemaNuevoId: String?,
            cerradoPor: String
        ): InformeDto = InformeDto(
            idOrden = informe.idOrden,
            solucionAplicada = informe.solucionAplicada,
            trabajos = informe.trabajos.map {
                TrabajoTecnicoDto(it.tecnicoId, it.nombreTecnico, it.tareaAsignada, it.trabajoRealizado)
            },
            problemaNuevoId = problemaNuevoId,
            fechaCierre = Timestamp.now(),
            cerradoPor = cerradoPor
        )
    }
}

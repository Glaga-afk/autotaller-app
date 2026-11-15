package com.example.autotallerapp.domain.model

/**
 * Trabajo de un tecnico dentro de la OST (HU08).
 * La tarea asignada viene de la HU06 (detalle_tecnicos); el trabajo realizado
 * lo escribe el tecnico supervisor al cerrar la orden.
 */
data class TrabajoTecnico(
    val tecnicoId: String = "",
    val nombreTecnico: String = "",
    val tareaAsignada: String = "",
    val trabajoRealizado: String = ""
)

/**
 * Informe tecnico con el que se cierra la OST (HU08).
 *
 * - solucionAplicada: detalle de la reparacion que se hizo.
 * - trabajos: un registro por cada tecnico que participo.
 * - problemaNuevo: si el problema no estaba en la bitacora, se registra junto con el cierre.
 *   Su "descripcion" es la causa y su "solucion" es la solucion aplicada.
 */
data class InformeTecnico(
    val idOrden: String,
    val solucionAplicada: String,
    val trabajos: List<TrabajoTecnico>,
    val problemaNuevo: ProblemaTecnico? = null
)

/** Datos que necesita la pantalla del informe: la OST y sus tecnicos asignados. */
data class DetalleCierre(
    val orden: OrdenServicio,
    val trabajos: List<TrabajoTecnico>
)

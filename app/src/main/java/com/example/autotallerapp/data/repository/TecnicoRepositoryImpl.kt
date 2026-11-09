package com.example.autotallerapp.data.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.local.dao.OrdenDao
import com.example.autotallerapp.data.local.dao.TecnicoDao
import com.example.autotallerapp.data.local.entity.TecnicoEntity
import com.example.autotallerapp.domain.model.AsignacionTecnico
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.Tecnico
import com.example.autotallerapp.domain.repository.TecnicoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TecnicoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val tecnicoDao: TecnicoDao,
    private val ordenDao: OrdenDao
) : TecnicoRepository {

    override suspend fun obtenerTecnicosDisponibles(): Resultado<List<Tecnico>> {
        return try {
            // 1. Consultar Firestore filtrando por los roles de técnico permitidos
            val snapshot = firestore.collection(COLECCION_USUARIOS)
                .whereIn("rol", listOf("TECNICO", "Técnico", "tecnico", "TECNICO_SUPERVISOR", "Tecnico_Supervisor"))
                .get()
                .await()

            val listaTecnicos = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val nombre = doc.getString("nombre") ?: doc.getString("nombreCompleto").orEmpty()
                val especialidad = doc.getString("especialidad") ?: "Mecánica General"
                val disponible = doc.getBoolean("disponible") ?: true

                if (nombre.isNotBlank()) {
                    Tecnico(
                        id = id,
                        nombre = nombre,
                        especialidad = especialidad,
                        disponible = disponible
                    )
                } else null
            }

            // 2. Guardar en Room para caché de emergencia
            tecnicoDao.insertarTodos(listaTecnicos.map { TecnicoEntity.desdeDominio(it) })

            Resultado.Exito(listaTecnicos)
        } catch (e: Exception) {
            // 3. Respaldo local en Room si no hay conexión
            try {
                val locales = tecnicoDao.obtenerDisponiblesLocales().map { it.aDominio() }
                Resultado.Exito(locales)
            } catch (dbError: Exception) {
                Resultado.Error("No se pudo obtener la lista de técnicos.")
            }
        }
    }

    override suspend fun asignarTecnicosAOrden(
        idOrdenLocal: String,
        asignaciones: List<AsignacionTecnico>
    ): Resultado<Unit> {
        return try {
            val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(idOrdenLocal)
            val lote = firestore.batch()

            asignaciones.forEach { asignacion ->
                // 1. Guardar el detalle de la asignación en la subcolección de la OST
                val refDetalle = referenciaOrden.collection(SUBCOLECCION_DETALLE_TECNICOS).document()
                lote.set(refDetalle, asignacion)

                // 2. Marcar al técnico como ocupado (disponible = false) en la colección 'usuarios'
                val refUsuario = firestore.collection(COLECCION_USUARIOS).document(asignacion.tecnicoId)
                lote.update(refUsuario, "disponible", false)
            }

            // 3. Actualizar el estado general de la OST a 'EN_REPARACION'
            lote.update(referenciaOrden, "estado", EstadoOrden.EN_REPARACION.name)

            // 4. Ejecutar todas las operaciones de forma atómica
            lote.commit().await()

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("No se pudo registrar la asignación en la nube: ${e.message}")
        }
    }

    override suspend fun actualizarEstadoOrdenYLiberarTecnicos(
        idOrdenLocal: String,
        nuevoEstado: EstadoOrden
    ): Resultado<Unit> {
        return try {
            val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(idOrdenLocal)
            val lote = firestore.batch()

            // 1. Consultar los técnicos que estuvieron asignados en esta orden
            val snapshotDetalle = referenciaOrden.collection(SUBCOLECCION_DETALLE_TECNICOS).get().await()
            val idsTecnicosAsignados = snapshotDetalle.documents.mapNotNull { it.getString("tecnicoId") }

            // 2. Liberar a cada técnico cambiando su disponibilidad a true
            idsTecnicosAsignados.forEach { tecnicoId ->
                val refUsuario = firestore.collection(COLECCION_USUARIOS).document(tecnicoId)
                lote.update(refUsuario, "disponible", true)
            }

            // 3. Actualizar el estado de la OST (ej. FINALIZADO o ENTREGADO)
            lote.update(referenciaOrden, "estado", nuevoEstado.name)

            lote.commit().await()
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("No se pudo actualizar la orden y liberar a los técnicos: ${e.message}")
        }
    }

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
        const val COLECCION_ORDENES = "ordenes"
        const val SUBCOLECCION_DETALLE_TECNICOS = "detalle_tecnicos"
    }
}
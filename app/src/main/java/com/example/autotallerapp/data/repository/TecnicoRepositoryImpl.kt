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
            // 1. Consultar Firestore filtrando por el rol de técnico (o en la colección 'usuarios'/'tecnicos')
            val snapshot = firestore.collection(COLECCION_USUARIOS)
                .whereIn("rol", listOf("TECNICO", "Técnico", "tecnico"))
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
            // 3. Respaldo de emergencia en Room si no hay conexión
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
            // 1. Guardar las asignaciones en la subcolección 'detalle_tecnicos' de la OST en Firestore
            val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(idOrdenLocal)

            val lote = firestore.batch()

            asignaciones.forEach { asignacion ->
                val refDetalle = referenciaOrden.collection(SUBCOLECCION_DETALLE_TECNICOS).document()
                lote.set(refDetalle, asignacion)
            }

            // 2. Actualizar el estado de la OST a 'EN_REPARACION' en Firestore
            lote.update(referenciaOrden, "estado", EstadoOrden.EN_REPARACION.name)

            lote.commit().await()

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("No se pudo registrar la asignación en la nube: ${e.message}")
        }
    }

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
        const val COLECCION_ORDENES = "ordenes"
        const val SUBCOLECCION_DETALLE_TECNICOS = "detalle_tecnicos"
    }
}
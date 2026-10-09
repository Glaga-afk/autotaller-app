package com.example.autotallerapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.local.dao.ProblemaDao
import com.example.autotallerapp.data.remote.dto.ProblemaTecnicoDto
import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.repository.BitacoraRepository
import javax.inject.Inject
import javax.inject.Singleton

class BitacoraRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val problemaDao: ProblemaDao
) : BitacoraRepository {
    override fun buscar(filtro: FiltroBitacora): Flow<List<ProblemaTecnico>> {
        val sistema = filtro.sistema?.name.orEmpty()
        val marca = filtro.marca.trim()
        val modelo = filtro.modelo.trim()

        val entidades = if (filtro.palabraClave.isBlank()) {
            problemaDao.buscarSinTexto(sistema, marca, modelo)
        } else {
            val consulta = filtro.palabraClave.trim().split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { "$it*" }
            problemaDao.buscarConTexto(consulta, sistema, marca, modelo)
        }

        return entidades.map { lista ->
            lista.map {
                ProblemaTecnico(
                    id = it.id,
                    sistema = SistemaAutomovil.desde(it.sistema),
                    marca = it.marca,
                    modelo = it.modelo,
                    titulo = it.titulo,
                    descripcion = it.descripcion,
                    solucion = it.solucion,
                    actualizadoEn = it.actualizadoEn
                )
            }
        }
    }

    override suspend fun sincronizar(): Resultado<Int> = try {
        val documentos = firestore.collection(COLECCION_BITACORA).get().await()
        val problemas = documentos.documents.mapNotNull { doc ->
            doc.toObject(ProblemaTecnicoDto::class.java)?.copy(id = doc.id)
        }
        problemaDao.reemplazarTodo(problemas.map { it.aEntity() })
        Resultado.Exito(problemas.size)
    } catch (e: Exception) {
        android.util.Log.e("BitacoraRepo", "Fallo la sincronizacion de la bitacora", e)
        Resultado.Error("No se pudo sincronizar la bitacora. Se sigue usando la copia local")
    }

    override suspend fun hayDatosLocales(): Boolean = problemaDao.contar() > 0

    private companion object {
        const val COLECCION_BITACORA = "bitacora"
    }
}
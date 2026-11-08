package com.example.autotallerapp.data.repository

import android.content.Context
import android.util.Log
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.local.GestorArchivosOrden
import com.example.autotallerapp.data.local.dao.OrdenDao
import com.example.autotallerapp.data.local.entity.OrdenEntity
import com.example.autotallerapp.data.remote.almacenamiento.AlmacenamientoArchivos
import com.example.autotallerapp.data.remote.dto.OrdenDto
import com.example.autotallerapp.domain.model.EstadoEnvio
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.TipoArchivo
import com.example.autotallerapp.domain.repository.OrdenRepository
import com.example.autotallerapp.work.ProgramadorSincronizacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdenRepositoryImpl @Inject constructor(
    @ApplicationContext private val contexto: Context,
    ordenDao: OrdenDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val almacenamiento: AlmacenamientoArchivos,
    private val gestorArchivos: GestorArchivosOrden
) : OrdenRepository {

    private val localOrdenDao = ordenDao

    override suspend fun registrar(orden: OrdenServicio): Resultado<OrdenServicio> = try {
        val idLocal = UUID.randomUUID().toString()
        val (fotos, audio) = gestorArchivos.guardarEvidencias(idLocal, orden.fotos, orden.audio)

        val completa = orden.copy(
            idLocal = idLocal,
            numero = null,
            estado = EstadoOrden.PENDIENTE,
            estadoEnvio = EstadoEnvio.PENDIENTE_ENVIO,
            fotos = fotos,
            audio = audio,
            fechaRegistro = System.currentTimeMillis(),
            registradoPor = auth.currentUser?.uid.orEmpty()
        )

        localOrdenDao.insertar(OrdenEntity.desdeDominio(completa))
        ProgramadorSincronizacion.enviarOrdenesPendientes(contexto)

        Resultado.Exito(completa)
    } catch (_: Exception) {
        Resultado.Error("No se pudo guardar la orden en el dispositivo")
    }

    override fun observar(idLocal: String): Flow<OrdenServicio?> =
        localOrdenDao.observar(idLocal).map { it?.aDominio() }

    override suspend fun sincronizarPendientes(): Resultado<Int> {
        val pendientes = localOrdenDao.obtenerPorEnviar()
        var enviadas = 0
        var fallidas = 0

        for (entidad in pendientes) {
            try {
                enviar(entidad.aDominio())
                enviadas++
            } catch (e: Exception) {
                localOrdenDao.marcarError(entidad.idLocal, e.message ?: "Error desconocido")
                fallidas++
            }
        }

        return if (fallidas == 0) {
            Resultado.Exito(enviadas)
        } else {
            Resultado.Error("$fallidas orden(es) no se pudieron enviar")
        }
    }

    private suspend fun enviar(orden: OrdenServicio) {
        val fotosRemotas = orden.fotos.mapIndexed { indice, ruta ->
            almacenamiento.subir(orden.idLocal, ruta, TipoArchivo.FOTO, indice + 1)
        }
        val audioRemoto = orden.audio?.let {
            almacenamiento.subir(orden.idLocal, it, TipoArchivo.AUDIO, 1)
        }

        val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(orden.idLocal)
        val referenciaContador = firestore.collection(COLECCION_CONTADORES).document(CONTADOR_OST)

        val numero = firestore.runTransaction { transaccion ->
            val documentoOrden = transaccion.get(referenciaOrden)
            val numeroPrevio = documentoOrden.getLong(CAMPO_NUMERO)

            if (numeroPrevio != null && numeroPrevio > 0) {
                numeroPrevio
            } else {
                val ultimo = transaccion.get(referenciaContador).getLong(CAMPO_ULTIMO) ?: 0L
                val nuevo = ultimo + 1
                transaccion.set(referenciaContador, mapOf(CAMPO_ULTIMO to nuevo))
                transaccion.set(
                    referenciaOrden,
                    OrdenDto.desdeDominio(orden, nuevo, fotosRemotas, audioRemoto)
                )
                nuevo
            }
        }.await()

        localOrdenDao.marcarEnviada(orden.idLocal, numero)
    }

    override suspend fun obtenerTodas(): Resultado<List<OrdenServicio>> {
        return try {
            val snapshot = firestore.collection(COLECCION_ORDENES).get().await()
            Log.d("OrdenRepoDebug", "Documentos en Firestore encontrados: ${snapshot.size()}")

            val listaOrdenes = snapshot.documents.mapNotNull { doc ->
                val idLocal = doc.getString("idLocal") ?: doc.id
                val numero = doc.getLong("numero") ?: doc.getDouble("numero")?.toLong()

                val estadoStr = doc.getString("estado") ?: "PENDIENTE"
                val estado = try {
                    EstadoOrden.valueOf(estadoStr)
                } catch (_: Exception) {
                    EstadoOrden.PENDIENTE
                }

                // 💡 Lectura robusta de fechaRegistro compatible con Timestamp de Firestore y Long
                val fechaRegistro = try {
                    val timestamp = doc.getTimestamp("fechaRegistro")
                    if (timestamp != null) {
                        timestamp.seconds * 1000
                    } else {
                        doc.getLong("fechaRegistro") ?: System.currentTimeMillis()
                    }
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }

                val registradoPor = doc.getString("registradoPor").orEmpty()
                val clienteNombre = doc.getString("clienteNombre").orEmpty()
                val placa = doc.getString("placa").orEmpty()
                val marca = doc.getString("marca").orEmpty()
                val modelo = doc.getString("modelo").orEmpty()
                val falla = doc.getString("falla").orEmpty()

                OrdenServicio(
                    idLocal = idLocal,
                    numero = numero,
                    estado = estado,
                    estadoEnvio = EstadoEnvio.PENDIENTE_ENVIO,
                    fotos = emptyList(),
                    audio = null,
                    fechaRegistro = fechaRegistro,
                    registradoPor = registradoPor,
                    clienteNombre = clienteNombre,
                    placa = placa,
                    marca = marca,
                    modelo = modelo,
                    falla = falla
                )
            }

            Log.d("OrdenRepoDebug", "Órdenes mapeadas con éxito: ${listaOrdenes.size}")

            listaOrdenes.forEach { orden ->
                localOrdenDao.insertar(OrdenEntity.desdeDominio(orden))
            }

            Resultado.Exito(listaOrdenes)
        } catch (e: Exception) {
            Log.e("OrdenRepoDebug", "EXCEPCIÓN al obtener órdenes de Firestore", e)
            try {
                val entidadesLocales = localOrdenDao.obtenerTodas()
                Log.d("OrdenRepoDebug", "Órdenes leídas de Room local: ${entidadesLocales.size}")
                val listaLocal = entidadesLocales.map { it.aDominio() }
                Resultado.Exito(listaLocal)
            } catch (dbError: Exception) {
                Log.e("OrdenRepoDebug", "EXCEPCIÓN al leer Room", dbError)
                Resultado.Error("No se pudieron cargar las órdenes ni desde la nube ni localmente.")
            }
        }
    }

    private companion object {
        const val COLECCION_ORDENES = "ordenes"
        const val COLECCION_CONTADORES = "contadores"
        const val CONTADOR_OST = "ordenes_servicio"
        const val CAMPO_NUMERO = "numero"
        const val CAMPO_ULTIMO = "ultimo"
    }
}
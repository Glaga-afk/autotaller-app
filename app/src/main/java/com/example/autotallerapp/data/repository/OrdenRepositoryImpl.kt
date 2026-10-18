package com.example.autotallerapp.data.repository

import android.content.Context
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
    private val ordenDao: OrdenDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val almacenamiento: AlmacenamientoArchivos,
    private val gestorArchivos: GestorArchivosOrden
) : OrdenRepository {

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

        // 1. Se guarda en Room: la orden ya existe aunque no haya internet
        ordenDao.insertar(OrdenEntity.desdeDominio(completa))
        // 2. WorkManager la enviara en cuanto haya conexion
        ProgramadorSincronizacion.enviarOrdenesPendientes(contexto)

        Resultado.Exito(completa)
    } catch (e: Exception) {
        Resultado.Error("No se pudo guardar la orden en el dispositivo")
    }

    override fun observar(idLocal: String): Flow<OrdenServicio?> =
        ordenDao.observar(idLocal).map { it?.aDominio() }

    override suspend fun sincronizarPendientes(): Resultado<Int> {
        val pendientes = ordenDao.obtenerPorEnviar()
        var enviadas = 0
        var fallidas = 0

        // Cada orden se envia por separado: si una falla, las demas siguen
        for (entidad in pendientes) {
            try {
                enviar(entidad.aDominio())
                enviadas++
            } catch (e: Exception) {
                ordenDao.marcarError(entidad.idLocal, e.message ?: "Error desconocido")
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
        // 1. Primero los archivos: si fallan, la orden no se publica a medias
        val fotosRemotas = orden.fotos.mapIndexed { indice, ruta ->
            almacenamiento.subir(orden.idLocal, ruta, TipoArchivo.FOTO, indice + 1)
        }
        val audioRemoto = orden.audio?.let {
            almacenamiento.subir(orden.idLocal, it, TipoArchivo.AUDIO, 1)
        }

        val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(orden.idLocal)
        val referenciaContador = firestore.collection(COLECCION_CONTADORES).document(CONTADOR_OST)

        // 2. Transaccion: lee el ultimo numero, lo incrementa y crea la orden en un solo paso.
        //    Si dos celulares envian a la vez, Firestore repite una de las transacciones,
        //    asi nunca se repite un numero.
        val numero = firestore.runTransaction { transaccion ->
            val documentoOrden = transaccion.get(referenciaOrden)
            val numeroPrevio = documentoOrden.getLong(CAMPO_NUMERO)

            if (numeroPrevio != null && numeroPrevio > 0) {
                // La orden ya se habia enviado en un intento anterior: se conserva su numero
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

        // 3. Se marca como enviada en Room; la pantalla recibe el numero por el Flow
        ordenDao.marcarEnviada(orden.idLocal, numero)
    }

    private companion object {
        const val COLECCION_ORDENES = "ordenes"
        const val COLECCION_CONTADORES = "contadores"
        const val CONTADOR_OST = "ordenes_servicio"
        const val CAMPO_NUMERO = "numero"
        const val CAMPO_ULTIMO = "ultimo"
    }
}

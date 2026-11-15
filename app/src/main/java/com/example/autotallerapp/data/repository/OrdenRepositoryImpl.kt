package com.example.autotallerapp.data.repository

import android.content.Context
import android.util.Log
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.local.GestorArchivosOrden
import com.example.autotallerapp.data.local.dao.OrdenDao
import com.example.autotallerapp.data.local.entity.OrdenEntity
import com.example.autotallerapp.data.remote.almacenamiento.AlmacenamientoArchivos
import com.example.autotallerapp.data.remote.dto.InformeDto
import com.example.autotallerapp.data.remote.dto.OrdenDto
import com.example.autotallerapp.data.remote.dto.ProblemaTecnicoDto
import com.example.autotallerapp.domain.model.AsignacionTecnico
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.model.EstadoEnvio
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.TipoArchivo
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.example.autotallerapp.domain.repository.OrdenRepository
import com.example.autotallerapp.work.ProgramadorSincronizacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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

    override suspend fun obtenerLocales(): Resultado<List<OrdenServicio>> {
        return try {
            val entidades = localOrdenDao.obtenerTodas()
            Resultado.Exito(entidades.map { it.aDominio() })
        } catch (e: Exception) {
            Resultado.Error("Error al cargar órdenes locales: ${e.message}")
        }
    }

    // ---------- HU08: informe tecnico y cierre de la OST ----------

    override suspend fun obtenerPorEstado(estado: EstadoOrden): Resultado<List<OrdenServicio>> = try {
        val documentos = firestore.collection(COLECCION_ORDENES)
            .whereEqualTo(CAMPO_ESTADO, estado.name)
            .get()
            .await()

        // Se ordena en el celular para no necesitar un indice compuesto en Firestore
        val ordenes = documentos.documents
            .mapNotNull { doc -> doc.toObject(OrdenDto::class.java)?.aDominio(doc.id) }
            .sortedBy { it.numero ?: Long.MAX_VALUE }

        Resultado.Exito(ordenes)
    } catch (_: Exception) {
        Resultado.Error("No se pudieron cargar las ordenes. Revisa tu conexion")
    }

    override suspend fun obtenerPorId(idOrden: String): Resultado<OrdenServicio> = try {
        val documento = firestore.collection(COLECCION_ORDENES).document(idOrden).get().await()
        val orden = documento.toObject(OrdenDto::class.java)?.aDominio(documento.id)
        if (orden != null) Resultado.Exito(orden) else Resultado.Error("La orden no existe")
    } catch (_: Exception) {
        Resultado.Error("No se pudo cargar la orden. Revisa tu conexion")
    }

    override suspend fun obtenerTrabajosAsignados(idOrden: String): Resultado<List<TrabajoTecnico>> = try {
        val documentos = firestore.collection(COLECCION_ORDENES).document(idOrden)
            .collection(SUBCOLECCION_DETALLE_TECNICOS)
            .get()
            .await()

        val trabajos = documentos.documents
            .mapNotNull { it.toObject(AsignacionTecnico::class.java) }
            .sortedBy { it.fechaAsignacion }
            // Si un tecnico se asigno dos veces, vale la ultima asignacion
            .associateBy { it.tecnicoId }
            .values
            .map { asignacion ->
                TrabajoTecnico(
                    tecnicoId = asignacion.tecnicoId,
                    nombreTecnico = asignacion.nombreTecnico,
                    tareaAsignada = asignacion.tareaAsignada
                )
            }

        Resultado.Exito(trabajos)
    } catch (_: Exception) {
        Resultado.Error("No se pudieron cargar los tecnicos asignados")
    }

    override suspend fun cerrarConInforme(informe: InformeTecnico): Resultado<ProblemaTecnico?> {
        val uid = auth.currentUser?.uid ?: return Resultado.Error("Tu sesion expiro. Vuelve a ingresar")

        return try {
            val referenciaOrden = firestore.collection(COLECCION_ORDENES).document(informe.idOrden)
            val referenciaInforme = referenciaOrden
                .collection(SUBCOLECCION_INFORME)
                .document(DOCUMENTO_INFORME)

            // Tecnicos que se liberan al cerrar la OST (igual que "Marcar Atendida" de la HU07)
            val referenciasTecnicos = informe.trabajos
                .map { it.tecnicoId }
                .filter { it.isNotBlank() }
                .distinct()
                .map { firestore.collection(COLECCION_USUARIOS).document(it) }

            // El id del problema se genera antes para poder guardarlo dentro del informe
            val referenciaProblema = informe.problemaNuevo?.let {
                firestore.collection(COLECCION_BITACORA).document()
            }
            val problemaFinal = informe.problemaNuevo?.copy(
                id = referenciaProblema?.id.orEmpty(),
                solucion = informe.solucionAplicada,
                actualizadoEn = System.currentTimeMillis()
            )

            // Transaccion: o se guarda todo (informe + bitacora + estado + tecnicos) o nada.
            // Ademas evita que dos supervisores cierren la misma OST a la vez.
            firestore.runTransaction { transaccion ->
                // 1. Lecturas (en una transaccion van antes que cualquier escritura)
                val documentoOrden = transaccion.get(referenciaOrden)
                if (!documentoOrden.exists()) {
                    throw CierreInvalidoException("La orden ya no existe")
                }
                when (EstadoOrden.desde(documentoOrden.getString(CAMPO_ESTADO))) {
                    EstadoOrden.EN_REPARACION -> Unit
                    EstadoOrden.ATENDIDA ->
                        throw CierreInvalidoException("Esta OST ya fue cerrada")
                    else ->
                        throw CierreInvalidoException("Solo se puede cerrar una OST que esta en reparacion")
                }
                // Solo se liberan los tecnicos que existen en "usuarios"
                val tecnicosExistentes = referenciasTecnicos.filter { transaccion.get(it).exists() }

                // 2. Escrituras
                transaccion.set(
                    referenciaInforme,
                    InformeDto.desdeDominio(informe, problemaFinal?.id, uid)
                )

                if (referenciaProblema != null && problemaFinal != null) {
                    transaccion.set(
                        referenciaProblema,
                        ProblemaTecnicoDto(
                            id = problemaFinal.id,
                            sistema = problemaFinal.sistema.name,
                            marca = problemaFinal.marca,
                            modelo = problemaFinal.modelo,
                            titulo = problemaFinal.titulo,
                            descripcion = problemaFinal.descripcion,
                            solucion = problemaFinal.solucion,
                            actualizadoEn = problemaFinal.actualizadoEn
                        )
                    )
                }

                tecnicosExistentes.forEach { transaccion.update(it, CAMPO_DISPONIBLE, true) }

                transaccion.update(
                    referenciaOrden,
                    mapOf(
                        CAMPO_ESTADO to EstadoOrden.ATENDIDA.name,
                        CAMPO_FECHA_CIERRE to FieldValue.serverTimestamp(),
                        CAMPO_CERRADO_POR to uid
                    )
                )
                true
            }.await()

            // Si la orden tambien esta en el Room de este celular, se actualiza su estado
            localOrdenDao.actualizarEstado(informe.idOrden, EstadoOrden.ATENDIDA.name)

            Resultado.Exito(problemaFinal)
        } catch (e: Exception) {
            Log.e("OrdenRepoDebug", "Error al cerrar la OST", e)
            Resultado.Error(mensajeDeCierre(e))
        }
    }

    override suspend fun registrarAvisoCliente(idOrden: String, canal: CanalAviso): Resultado<Unit> = try {
        firestore.collection(COLECCION_ORDENES).document(idOrden)
            .update(
                mapOf(
                    "avisoCliente.canal" to canal.name,
                    "avisoCliente.fecha" to FieldValue.serverTimestamp(),
                    "avisoCliente.avisadoPor" to auth.currentUser?.uid.orEmpty()
                )
            )
            .await()
        Resultado.Exito(Unit)
    } catch (e: Exception) {
        Resultado.Error("No se pudo registrar el aviso al cliente")
    }

    /** Busca en la cadena de causas el motivo real del error para mostrar un mensaje claro. */
    private fun mensajeDeCierre(error: Throwable): String {
        var actual: Throwable? = error
        while (actual != null) {
            if (actual is CierreInvalidoException) return actual.message.orEmpty()
            if (actual is FirebaseFirestoreException) {
                when (actual.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        return "No tienes permiso para cerrar la OST. Revisa tu rol y las reglas de Firestore"
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        return "Sin conexion: el cierre de la OST necesita internet"
                    else -> Unit
                }
            }
            actual = actual.cause
        }
        return "No se pudo cerrar la OST. Intentalo de nuevo"
    }

    private companion object {
        const val COLECCION_ORDENES = "ordenes"
        const val COLECCION_CONTADORES = "contadores"
        const val CONTADOR_OST = "ordenes_servicio"
        const val CAMPO_NUMERO = "numero"
        const val CAMPO_ULTIMO = "ultimo"

        // HU08
        const val COLECCION_BITACORA = "bitacora"
        const val COLECCION_USUARIOS = "usuarios"
        const val SUBCOLECCION_DETALLE_TECNICOS = "detalle_tecnicos"
        const val SUBCOLECCION_INFORME = "informe"
        const val DOCUMENTO_INFORME = "final"
        const val CAMPO_ESTADO = "estado"
        const val CAMPO_FECHA_CIERRE = "fechaCierre"
        const val CAMPO_CERRADO_POR = "cerradoPor"
        const val CAMPO_DISPONIBLE = "disponible"
    }
}

/** Error de negocio lanzado dentro de la transaccion de cierre (HU08). */
private class CierreInvalidoException(mensaje: String) : Exception(mensaje)

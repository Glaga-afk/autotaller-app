package com.example.autotallerapp.data.repository

import android.util.Base64
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.remote.dto.OrdenDto
import com.example.autotallerapp.data.remote.ia.GeminiConfig
import com.example.autotallerapp.domain.model.DiagnosticoOrden
import com.example.autotallerapp.domain.model.EstadoEnvio
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.FotoEvidencia
import com.example.autotallerapp.domain.model.OrdenConEvidencia
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.Ubicacion
import com.example.autotallerapp.domain.repository.DiagnosticoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class DiagnosticoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val http: OkHttpClient
) : DiagnosticoRepository {

    override suspend fun obtenerOrdenesRecientes(): Resultado<List<OrdenServicio>> = try {
        val documentos = firestore.collection(COLECCION_ORDENES)
            .orderBy("fechaRegistro", Query.Direction.DESCENDING)
            .limit(LIMITE_ORDENES)
            .get()
            .await()

        val ordenes = documentos.documents.mapNotNull { doc ->
            doc.toObject(OrdenDto::class.java)?.copy(id = doc.id)?.aOrdenServicio()
        }
        Resultado.Exito(ordenes)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(traducir(e))
    }

    override suspend fun obtenerOrdenConFotos(ordenId: String): Resultado<OrdenConEvidencia> = try {
        val documento = firestore.collection(COLECCION_ORDENES).document(ordenId).get().await()
        val orden = documento.toObject(OrdenDto::class.java)
            ?.copy(id = documento.id)
            ?.aOrdenServicio()

        if (orden == null) {
            Resultado.Error("No se encontro la orden")
        } else {
            val fotos = orden.fotos.mapNotNull { leerFoto(it) }
            if (fotos.isEmpty()) {
                Resultado.Error("No se pudieron cargar las fotos de la orden")
            } else {
                Resultado.Exito(OrdenConEvidencia(orden, fotos))
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(traducir(e))
    }

    /** Una orden tiene un unico diagnostico vigente: guardar de nuevo lo reemplaza (sin duplicados). */
    override suspend fun guardar(diagnostico: DiagnosticoOrden): Resultado<Unit> = try {
        val sugerencia = diagnostico.sugerenciaIa
        val datos = mapOf(
            "ordenId" to diagnostico.ordenId,
            "sistema" to diagnostico.sistema.name,
            "causaProbable" to diagnostico.causaProbable,
            "urgencia" to diagnostico.urgencia.name,
            "sugerenciaIa" to mapOf(
                "sistema" to sugerencia.sistema.name,
                "causaProbable" to sugerencia.causaProbable,
                "urgencia" to sugerencia.urgencia.name,
                "palabrasClave" to sugerencia.palabrasClave
            ),
            "corregidoPorSupervisor" to diagnostico.corregidoPorSupervisor,
            "modeloIa" to GeminiConfig.modelo,
            "creadoPor" to auth.currentUser?.uid.orEmpty(),
            "creadoEn" to FieldValue.serverTimestamp()
        )

        // Sin conexion, Firestore deja la tarea esperando: se corta para avisar al usuario
        withTimeout(TIEMPO_GUARDADO_MS) {
            firestore.collection(COLECCION_ORDENES)
                .document(diagnostico.ordenId)
                .collection(SUBCOLECCION_DIAGNOSTICOS)
                .document(DOCUMENTO_VIGENTE)
                .set(datos)
                .await()
        }
        Resultado.Exito(Unit)
    } catch (e: TimeoutCancellationException) {
        Resultado.Error("No se pudo guardar. Revisa tu conexion e intentalo de nuevo")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(traducir(e))
    }

    // ---------- Lectura de fotos ----------

    /** Soporta las dos formas de guardar una foto: firestore:// (plan Spark) y https:// (Storage, plan Blaze). */
    private suspend fun leerFoto(referencia: String): FotoEvidencia? = try {
        when {
            referencia.startsWith(ESQUEMA_FIRESTORE) -> {
                val doc = firestore.document(referencia.removePrefix(ESQUEMA_FIRESTORE)).get().await()
                val base64 = doc.getString("contenidoBase64")
                if (base64.isNullOrEmpty()) null else FotoEvidencia(base64, doc.getString("mime") ?: MIME_JPEG)
            }
            referencia.startsWith("http") -> descargar(referencia)
            else -> null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private suspend fun descargar(url: String): FotoEvidencia? = withContext(Dispatchers.IO) {
        http.newCall(Request.Builder().url(url).build()).execute().use { respuesta ->
            if (!respuesta.isSuccessful) return@use null
            val bytes = respuesta.body?.bytes() ?: return@use null
            FotoEvidencia(
                base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                mime = respuesta.header("Content-Type") ?: MIME_JPEG
            )
        }
    }

    // ---------- Mapeo y errores ----------

    /** La orden vive en Firestore, asi que ya esta enviada; las fotos son referencias remotas. */
    private fun OrdenDto.aOrdenServicio() = OrdenServicio(
        idLocal = id,
        numero = numero.takeIf { it > 0 },
        estado = EstadoOrden.desde(estado),
        estadoEnvio = EstadoEnvio.ENVIADA,
        clienteDni = clienteDni,
        clienteNombre = clienteNombre,
        automovilId = automovilId,
        placa = placa,
        marca = marca,
        modelo = modelo,
        falla = falla,
        fotos = fotos,
        audio = audio,
        ubicacion = if (latitud != null && longitud != null) Ubicacion(latitud, longitud) else null,
        fechaRegistro = fechaRegistro.toDate().time,
        registradoPor = registradoPor
    )

    private fun traducir(e: Exception): String =
        if (e is FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "No tienes permiso para esta operacion. Contacta al administrador"
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Sin conexion. Revisa tu internet e intentalo de nuevo"
                else -> "No se pudo completar la operacion. Intentalo de nuevo"
            }
        } else {
            "No se pudo completar la operacion. Intentalo de nuevo"
        }

    private companion object {
        const val COLECCION_ORDENES = "ordenes"
        const val SUBCOLECCION_DIAGNOSTICOS = "diagnosticos"
        const val DOCUMENTO_VIGENTE = "actual"
        const val ESQUEMA_FIRESTORE = "firestore://"
        const val MIME_JPEG = "image/jpeg"
        const val LIMITE_ORDENES = 30L
        const val TIEMPO_GUARDADO_MS = 15_000L
    }
}
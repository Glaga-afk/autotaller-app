package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.TrabajoTecnico
import kotlinx.coroutines.flow.Flow

interface OrdenRepository {

    /** Guarda la orden en el celular (funciona sin conexion) y programa su envio. */
    suspend fun registrar(orden: OrdenServicio): Resultado<OrdenServicio>

    /** Observa una orden local; sirve para mostrar su numero cuando se termine de enviar. */
    fun observar(idLocal: String): Flow<OrdenServicio?>

    /** Envia a Firestore todas las ordenes que siguen en el celular. Devuelve cuantas se enviaron. */
    suspend fun sincronizarPendientes(): Resultado<Int>

    suspend fun obtenerTodas(): Resultado<List<OrdenServicio>>

    /** Obtiene las órdenes almacenadas localmente en Room (soporte offline-first). */
    suspend fun obtenerLocales(): Resultado<List<OrdenServicio>>

    // ---------- HU08: informe tecnico y cierre de la OST ----------

    /** Ordenes de Firestore que estan en un estado (por ejemplo EN_REPARACION). */
    suspend fun obtenerPorEstado(estado: EstadoOrden): Resultado<List<OrdenServicio>>

    /** Una orden leida desde Firestore. */
    suspend fun obtenerPorId(idOrden: String): Resultado<OrdenServicio>

    /** Tecnicos asignados a la OST en la HU06 (subcoleccion detalle_tecnicos). */
    suspend fun obtenerTrabajosAsignados(idOrden: String): Resultado<List<TrabajoTecnico>>

    /**
     * Cierra la OST en una sola transaccion: guarda el informe, registra el problema
     * nuevo en la bitacora (si viene) y cambia el estado a ATENDIDA.
     * Devuelve el problema registrado, con su id, o null si no se registro ninguno.
     */
    suspend fun cerrarConInforme(informe: InformeTecnico): Resultado<ProblemaTecnico?>

    /** Guarda en la OST que se aviso al cliente que su vehiculo esta listo. */
    suspend fun registrarAvisoCliente(idOrden: String, canal: CanalAviso): Resultado<Unit>
}

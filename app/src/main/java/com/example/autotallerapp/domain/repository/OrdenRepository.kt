package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.OrdenServicio
import kotlinx.coroutines.flow.Flow

interface OrdenRepository {

    /** Guarda la orden en el celular (funciona sin conexion) y programa su envio. */
    suspend fun registrar(orden: OrdenServicio): Resultado<OrdenServicio>

    /** Observa una orden local; sirve para mostrar su numero cuando se termine de enviar. */
    fun observar(idLocal: String): Flow<OrdenServicio?>

    /** Envia a Firestore todas las ordenes que siguen en el celular. Devuelve cuantas se enviaron. */
    suspend fun sincronizarPendientes(): Resultado<Int>
}

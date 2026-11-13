package com.example.autotallerapp.data.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.local.dao.MensajeChatDao
import com.example.autotallerapp.data.local.entity.MensajeChatEntity
import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.repository.HistorialChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class HistorialChatRepositoryImpl @Inject constructor(
    private val dao: MensajeChatDao
) : HistorialChatRepository {
    override fun observar(conversacionId: String): Flow<List<MensajeChat>> =
        dao.observar(conversacionId).map { lista -> lista.map { it.aDominio() } }

    override suspend fun guardar(conversacionId: String, mensaje: MensajeChat): Resultado<Unit> = try {
        dao.insertar(MensajeChatEntity.desdeDominio(conversacionId, mensaje))
        Resultado.Exito(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error("No se pudo guardar el historial en el dispositivo")
    }

    override suspend fun borrar(conversacionId: String): Resultado<Unit> = try {
        dao.eliminarConversacion(conversacionId)
        Resultado.Exito(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error("No se pudo borrar el historial")
    }
}
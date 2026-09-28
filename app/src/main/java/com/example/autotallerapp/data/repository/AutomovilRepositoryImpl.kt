package com.example.autotallerapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.remote.dto.AutomovilDto
import com.example.autotallerapp.domain.model.Automovil
import com.example.autotallerapp.domain.repository.AutomovilRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomovilRepositoryImpl @Inject constructor (
    private val firestore: FirebaseFirestore
) : AutomovilRepository {
    override suspend fun existePlaca(placa: String): Boolean = try {
        val resultado = firestore.collectionGroup(SUBCOLECCION_AUTOMOVILES)
            .whereEqualTo("placa", placa)
            .limit(1)
            .get()
            .await()
        !resultado.isEmpty
    } catch (e: Exception) {
        false
    }

    override suspend fun registrar(automovil: Automovil): Resultado<Automovil> = try {
        val documento = firestore.collection(COLECCION_CLIENTES)
            .document(automovil.clienteId)
            .collection(SUBCOLECCION_AUTOMOVILES)
            .document()

        val conId = automovil.copy(id = documento.id)
        documento.set(AutomovilDto.desdeDominio(conId)).await()
        Resultado.Exito(conId)
    } catch (e: Exception) {
        Resultado.Error("No se pudo registrar el automovil. Intentalo de nuevo")
    }

    override suspend fun obtenerPorCliente(clienteId: String): Resultado<List<Automovil>> = try {
        val documentos = firestore.collection(COLECCION_CLIENTES)
            .document(clienteId)
            .collection(SUBCOLECCION_AUTOMOVILES)
            .get()
            .await()

        val automoviles = documentos.documents.mapNotNull { doc ->
            doc.toObject(AutomovilDto::class.java)?.copy(id = doc.id)?.aDominio()
        }
        Resultado.Exito(automoviles)
    } catch (e: Exception) {
        Resultado.Error("No se pudieron cargar los automoviles del cliente")
    }

    private companion object {
        const val COLECCION_CLIENTES = "clientes"
        const val SUBCOLECCION_AUTOMOVILES = "automoviles"
    }
}
package com.example.autotallerapp.data.repository

import com.example.autotallerapp.data.local.dao.ClienteDao
import com.example.autotallerapp.data.local.entity.ClienteEntity
import com.example.autotallerapp.data.remote.dto.ClienteDto
import com.example.autotallerapp.domain.model.Cliente
import com.example.autotallerapp.domain.repository.ClienteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ClienteRepositoryImpl @Inject constructor(
    private val clienteDao: ClienteDao,
    private val firestore: FirebaseFirestore
) : ClienteRepository {

    override suspend fun buscarClientePorDni(dni: String): Result<Cliente?> {
        return try {
            val local = clienteDao.buscarPorDni(dni)
            if (local != null) {
                return Result.success(Cliente(local.dni, local.nombreCompleto, local.direccion, local.sexo, local.telefono))
            }
            val doc = firestore.collection("clientes").document(dni).get().await()
            if (doc.exists()) {
                val dto = doc.toObject(ClienteDto::class.java)
                if (dto != null) {
                    clienteDao.insertarCliente(ClienteEntity(dto.dni, dto.nombreCompleto, dto.direccion, dto.sexo, dto.telefono))
                    return Result.success(Cliente(dto.dni, dto.nombreCompleto, dto.direccion, dto.sexo, dto.telefono))
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            val localFallback = clienteDao.buscarPorDni(dni)
            if (localFallback != null) {
                Result.success(Cliente(localFallback.dni, localFallback.nombreCompleto, localFallback.direccion, localFallback.sexo, localFallback.telefono))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun registrarCliente(cliente: Cliente): Result<Unit> {
        return try {
            val entity = ClienteEntity(cliente.dni, cliente.nombreCompleto, cliente.direccion, cliente.sexo, cliente.telefono)
            val dto = ClienteDto(cliente.dni, cliente.nombreCompleto, cliente.direccion, cliente.sexo, cliente.telefono)

            clienteDao.insertarCliente(entity)
            firestore.collection("clientes").document(cliente.dni).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
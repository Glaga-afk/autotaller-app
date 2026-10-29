package com.example.autotallerapp.data.local.dao

import androidx.room.*
import com.example.autotallerapp.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes")
    fun obtenerTodosLosClientes(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE dni = :dni LIMIT 1")
    suspend fun buscarPorDni(dni: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCliente(cliente: ClienteEntity)
}
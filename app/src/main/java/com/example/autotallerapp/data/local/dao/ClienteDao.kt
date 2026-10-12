package com.example.autotallerapp.data.local.dao

import androidx.room.*
import com.example.autotallerapp.data.local.entity.ClienteEntity

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes WHERE dni = :dni LIMIT 1")
    suspend fun buscarPorDni(dni: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCliente(cliente: ClienteEntity)
}
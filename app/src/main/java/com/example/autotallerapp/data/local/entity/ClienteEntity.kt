package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val dni: String,
    val nombreCompleto: String,
    val direccion: String,
    val sexo: String,
    val telefono: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)
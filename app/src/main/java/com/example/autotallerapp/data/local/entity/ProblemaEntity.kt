package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "problemas_fts")
data class ProblemaEntity(
    val id: String,
    val sistema: String,
    val marca: String,
    val modelo: String,
    val titulo: String,
    val descripcion: String,
    val solucion: String,
    val actualizadoEn: Long
)

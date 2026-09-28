package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marcas")
data class MarcaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)

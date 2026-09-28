package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "modelos",
    foreignKeys = [
        ForeignKey(
            entity = MarcaEntity::class,
            parentColumns = ["id"],
            childColumns = ["marcaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("marcaId")]
)
data class ModeloEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val marcaId: Int,
    val nombre: String
)

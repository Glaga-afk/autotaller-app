package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autotallerapp.domain.model.Tecnico

@Entity(tableName = "tecnicos")
data class TecnicoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val especialidad: String,
    val disponible: Boolean
) {
    fun aDominio(): Tecnico = Tecnico(
        id = id,
        nombre = nombre,
        especialidad = especialidad,
        disponible = disponible
    )

    companion object {
        fun desdeDominio(tecnico: Tecnico): TecnicoEntity = TecnicoEntity(
            id = tecnico.id,
            nombre = tecnico.nombre,
            especialidad = tecnico.especialidad,
            disponible = tecnico.disponible
        )
    }
}
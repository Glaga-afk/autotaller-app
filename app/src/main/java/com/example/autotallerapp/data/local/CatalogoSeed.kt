package com.example.autotallerapp.data.local

import com.example.autotallerapp.data.local.entity.MarcaEntity
import com.example.autotallerapp.data.local.entity.ModeloEntity

object CatalogoSeed {
    suspend fun sembrarSiVacio(db: AutoTallerDatabase) {
        val marcaDao = db.marcaDao()
        val modeloDao = db.modeloDao()

        if (marcaDao.contar() > 0) return

        val marcas = listOf(
            "Toyota", "Hyundai", "Kia", "Chevrolet", "Nissan",
            "Suzuki", "Volkswagen", "Mitsubishi", "Honda", "Ford"
        ).mapIndexed { indice, nombre -> MarcaEntity(id = indice + 1, nombre = nombre) }
        marcaDao.insertar(marcas)

        val modelosPorMarca = mapOf(
            "Toyota" to listOf("Yaris", "Corolla", "Hilux", "RAV4", "Land Cruiser"),
            "Hyundai" to listOf("Accent", "Elantra", "Tucson", "Santa Fe", "Grand i10"),
            "Kia" to listOf("Rio", "Sportage", "Sorento", "Picanto", "Cerato"),
            "Chevrolet" to listOf("Sail", "Spark", "Onix", "Tracker", "N300"),
            "Nissan" to listOf("Versa", "Sentra", "Frontier", "X-Trail", "March"),
            "Suzuki" to listOf("Swift", "Baleno", "Vitara", "S-Presso", "Ertiga"),
            "Volkswagen" to listOf("Gol", "Virtus", "Polo", "T-Cross", "Amarok"),
            "Mitsubishi" to listOf("Lancer", "Montero Sport", "L200", "ASX", "Outlander"),
            "Honda" to listOf("Civic", "CR-V", "Fit", "HR-V", "City"),
            "Ford" to listOf("Fiesta", "Ranger", "Escape", "EcoSport", "Focus")
        )

        val modelos = marcas.flatMap { marca ->
            modelosPorMarca[marca.nombre].orEmpty().map { nombreModelo ->
                ModeloEntity(marcaId = marca.id, nombre = nombreModelo)
            }
        }
        modeloDao.insertar(modelos)
    }
}
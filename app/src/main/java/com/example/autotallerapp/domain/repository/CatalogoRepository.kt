package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.model.Modelo
import kotlinx.coroutines.flow.Flow

interface CatalogoRepository {
    fun marcas(): Flow<List<Marca>>
    fun modelosPorMarca(marcaId: Int): Flow<List<Modelo>>
}
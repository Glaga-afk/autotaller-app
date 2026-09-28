package com.example.autotallerapp.data.repository

import com.example.autotallerapp.data.local.dao.MarcaDao
import com.example.autotallerapp.data.local.dao.ModeloDao
import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.model.Modelo
import com.example.autotallerapp.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogoRepositoryImpl @Inject constructor(
    private val marcaDao: MarcaDao,
    private val modeloDao: ModeloDao
) : CatalogoRepository {

    override fun marcas(): Flow<List<Marca>> =
        marcaDao.obtenerTodas().map { entidades ->
            entidades.map { Marca(id = it.id, nombre = it.nombre) }
        }

    override fun modelosPorMarca(marcaId: Int): Flow<List<Modelo>> =
        modeloDao.obtenerPorMarca(marcaId).map { entidades ->
            entidades.map { Modelo(id = it.id, marcaId = it.marcaId, nombre = it.nombre) }
        }

}
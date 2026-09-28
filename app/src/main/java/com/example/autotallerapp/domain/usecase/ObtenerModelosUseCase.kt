package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Modelo
import com.example.autotallerapp.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerModelosUseCase @Inject constructor (
    private val repository: CatalogoRepository
) {
    operator fun invoke(marcaId: Int): Flow<List<Modelo>> = repository.modelosPorMarca(marcaId)
}
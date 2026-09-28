package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerMarcasUseCase @Inject constructor (
    private val repository: CatalogoRepository
) {
    operator fun invoke(): Flow<List<Marca>> = repository.marcas()
}
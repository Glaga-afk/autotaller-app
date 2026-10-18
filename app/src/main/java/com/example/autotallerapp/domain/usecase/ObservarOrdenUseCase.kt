package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarOrdenUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    operator fun invoke(idLocal: String): Flow<OrdenServicio?> = repository.observar(idLocal)
}

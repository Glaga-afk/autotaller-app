package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BuscarProblemasUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    operator fun invoke(filtro: FiltroBitacora): Flow<List<ProblemaTecnico>> = repository.buscar(filtro)
}
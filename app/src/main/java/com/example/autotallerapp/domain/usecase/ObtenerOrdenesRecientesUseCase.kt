package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.DiagnosticoRepository
import javax.inject.Inject

class ObtenerOrdenesRecientesUseCase @Inject constructor(
    private val repository: DiagnosticoRepository
)  {
    suspend operator fun invoke(): Resultado<List<OrdenServicio>> =
        repository.obtenerOrdenesRecientes()
}
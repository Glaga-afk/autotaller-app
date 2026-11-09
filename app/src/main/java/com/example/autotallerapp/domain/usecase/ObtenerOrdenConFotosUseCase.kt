package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.OrdenConEvidencia
import com.example.autotallerapp.domain.repository.DiagnosticoRepository
import javax.inject.Inject

class ObtenerOrdenConFotosUseCase @Inject constructor(
    private val repository: DiagnosticoRepository
) {
    suspend operator fun invoke(ordenId: String): Resultado<OrdenConEvidencia> {
        if (ordenId.isBlank()) return Resultado.Error("No se indico la orden a diagnosticar")
        return repository.obtenerOrdenConFotos(ordenId)
    }
}
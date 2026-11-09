package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.FotoEvidencia
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.SugerenciaDiagnostico
import com.example.autotallerapp.domain.repository.DiagnosticoIARepository
import javax.inject.Inject

class DiagnosticarConIAUseCase @Inject constructor(
    private val repository: DiagnosticoIARepository
) {
    suspend operator fun invoke(
        orden: OrdenServicio,
        fotos: List<FotoEvidencia>
    ): Resultado<SugerenciaDiagnostico> {
        if (orden.falla.isBlank()) return Resultado.Error("La orden no tiene descripcion de la falla")
        if (fotos.isEmpty()) return Resultado.Error("La orden no tiene fotos para analizar")

        return repository.diagnosticar(
            falla = orden.falla.trim(),
            marca = orden.marca,
            modelo = orden.modelo,
            fotos = fotos.take(MAX_FOTOS)
        )
    }

    companion object {
        const val MAX_FOTOS = 3
    }
}
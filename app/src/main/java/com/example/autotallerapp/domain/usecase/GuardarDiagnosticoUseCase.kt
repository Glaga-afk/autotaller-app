package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.DiagnosticoOrden
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.SugerenciaDiagnostico
import com.example.autotallerapp.domain.model.Urgencia
import com.example.autotallerapp.domain.repository.DiagnosticoRepository
import javax.inject.Inject

class GuardarDiagnosticoUseCase @Inject constructor(
    private val repository: DiagnosticoRepository
) {
    suspend operator fun invoke(
        ordenId: String,
        sugerencia: SugerenciaDiagnostico,
        sistema: SistemaAutomovil,
        causaProbable: String,
        urgencia: Urgencia
    ): Resultado<Unit> {
        val causa = causaProbable.trim()
        if (causa.length < MIN_CARACTERES_CAUSA) {
            return Resultado.Error("Describe la causa probable (minimo $MIN_CARACTERES_CAUSA caracteres)")
        }

        val corregido = sistema != sugerencia.sistema ||
                causa != sugerencia.causaProbable.trim() ||
                urgencia != sugerencia.urgencia

        return repository.guardar(
            DiagnosticoOrden(
                ordenId = ordenId,
                sistema = sistema,
                causaProbable = causa,
                urgencia = urgencia,
                sugerenciaIa = sugerencia,
                corregidoPorSupervisor = corregido
            )
        )
    }

    companion object {
        const val MIN_CARACTERES_CAUSA = 5
    }
}
package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.FotoEvidencia
import com.example.autotallerapp.domain.model.SugerenciaDiagnostico

interface DiagnosticoIARepository {
    suspend fun diagnosticar(
        falla: String,
        marca: String,
        modelo: String,
        fotos: List<FotoEvidencia>
    ): Resultado<SugerenciaDiagnostico>
}
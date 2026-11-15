package com.example.autotallerapp.domain.repository

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import kotlinx.coroutines.flow.Flow

interface BitacoraRepository {
    // Siempre consulta la copia local (Room), nunca Firestore directamente
    fun buscar(filtro: FiltroBitacora): Flow<List<ProblemaTecnico>>
    suspend fun sincronizar(): Resultado<Int>
    suspend fun hayDatosLocales(): Boolean

    /** HU08: agrega a la copia local un problema que ya se guardo en Firestore. */
    suspend fun guardarLocal(problema: ProblemaTecnico)
}
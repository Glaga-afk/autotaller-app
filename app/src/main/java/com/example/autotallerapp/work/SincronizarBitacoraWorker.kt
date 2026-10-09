package com.example.autotallerapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.usecase.SincronizarBitacoraUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class SincronizarBitacoraWorker @AssistedInject constructor(
    @Assisted contexto: Context,
    @Assisted parametros: WorkerParameters,
    private val sincronizarBitacora: SincronizarBitacoraUseCase
) : CoroutineWorker(contexto, parametros) {
    override suspend fun doWork(): Result =
        when (sincronizarBitacora()) {
            is Resultado.Exito -> Result.success()
            is Resultado.Error -> Result.retry()
        }

    companion object {
        const val NOMBRE_TRABAJO = "sincronizar_bitacora"
    }
}
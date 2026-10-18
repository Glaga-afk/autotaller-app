package com.example.autotallerapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.usecase.SincronizarOrdenesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Envia a Firestore las ordenes registradas sin conexion.
 * WorkManager lo ejecuta solo cuando hay internet y lo reintenta si falla,
 * incluso si la app se cerro o el celular se reinicio.
 */
@HiltWorker
class SincronizarOrdenesWorker @AssistedInject constructor(
    @Assisted contexto: Context,
    @Assisted parametros: WorkerParameters,
    private val sincronizarOrdenes: SincronizarOrdenesUseCase
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result =
        when (sincronizarOrdenes()) {
            is Resultado.Exito -> Result.success()
            is Resultado.Error ->
                if (runAttemptCount < MAX_INTENTOS) Result.retry() else Result.failure()
        }

    companion object {
        const val NOMBRE_TRABAJO = "enviar_ordenes"
        private const val MAX_INTENTOS = 5
    }
}

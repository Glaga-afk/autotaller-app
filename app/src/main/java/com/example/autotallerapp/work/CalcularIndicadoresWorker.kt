package com.example.autotallerapp.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.repository.OrdenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalcularIndicadoresWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ordenRepository: OrdenRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("CalcularIndicadoresWorker", "Iniciando cálculo periódico de indicadores...")
            when (val resultado = ordenRepository.obtenerTodas()) {
                is Resultado.Exito -> {
                    val ordenes = resultado.dato
                    val total = ordenes.size
                    val porEstado = ordenes.groupingBy { it.estado }.eachCount()
                    Log.d("CalcularIndicadoresWorker", "Indicadores calculados exitosamente: Total=$total, Estados=$porEstado")
                    Result.success()
                }
                is Resultado.Error -> {
                    Log.w("CalcularIndicadoresWorker", "Error al obtener órdenes para indicadores: ${resultado.mensaje}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("CalcularIndicadoresWorker", "Excepción calculando indicadores", e)
            Result.failure()
        }
    }
}

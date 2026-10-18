package com.example.autotallerapp.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProgramadorSincronizacion {
    /** Repite la sincronizacion cada 6 horas mientras haya internet */
    fun programarPeriodica(contexto: Context) {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = PeriodicWorkRequestBuilder<SincronizarBitacoraWorker>(6, TimeUnit.HOURS)
            .setConstraints(restricciones)
            .build()

        WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
            SincronizarBitacoraWorker.NOMBRE_TRABAJO,
            ExistingPeriodicWorkPolicy.KEEP,
            solicitud
        )
    }

    /** Una sincronizacion inmediata, para no esperar horas la primera vez */
    fun sincronizarAhora(contexto: Context) {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<SincronizarBitacoraWorker>()
            .setConstraints(restricciones)
            .build()

        WorkManager.getInstance(contexto).enqueue(solicitud)
    }

    /**
     * HU05: envia las ordenes registradas sin conexion.
     * APPEND_OR_REPLACE encadena el envio detras del que este en curso,
     * asi una orden registrada mientras se envian otras no se pierde.
     */
    fun enviarOrdenesPendientes(contexto: Context) {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<SincronizarOrdenesWorker>()
            .setConstraints(restricciones)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(contexto).enqueueUniqueWork(
            SincronizarOrdenesWorker.NOMBRE_TRABAJO,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            solicitud
        )
    }
}
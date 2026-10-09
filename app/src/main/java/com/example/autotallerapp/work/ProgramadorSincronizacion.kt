package com.example.autotallerapp.work

import android.content.Context
import androidx.work.Constraints
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
}
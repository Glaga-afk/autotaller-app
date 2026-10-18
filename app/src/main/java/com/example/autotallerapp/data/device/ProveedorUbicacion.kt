package com.example.autotallerapp.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.autotallerapp.domain.model.Ubicacion
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Obtiene la ubicacion del vehiculo con FusedLocationProvider.
 * Se usa cuando la atencion es un auxilio a domicilio.
 */
class ProveedorUbicacion @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val cliente by lazy { LocationServices.getFusedLocationProviderClient(contexto) }

    fun tienePermiso(): Boolean = PERMISOS.any {
        ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Devuelve la ubicacion actual, o la ultima conocida si el GPS tarda demasiado. */
    @SuppressLint("MissingPermission")
    suspend fun obtenerActual(): Ubicacion? {
        if (!tienePermiso()) return null
        val cancelacion = CancellationTokenSource()

        return try {
            val actual = withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
                cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelacion.token).await()
            }
            val ubicacion = actual ?: cliente.lastLocation.await()
            ubicacion?.let { Ubicacion(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        } finally {
            cancelacion.cancel()
        }
    }

    companion object {
        val PERMISOS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val TIEMPO_MAXIMO_MS = 10_000L
    }
}

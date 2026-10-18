package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

class RegistrarOrdenUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    suspend operator fun invoke(orden: OrdenServicio): Resultado<OrdenServicio> {
        val falla = orden.falla.trim()

        return when {
            orden.clienteDni.isBlank() || orden.automovilId.isBlank() ->
                Resultado.Error("Selecciona el cliente y su automovil")
            falla.length < MIN_CARACTERES_FALLA ->
                Resultado.Error("Describe la falla con al menos $MIN_CARACTERES_FALLA caracteres")
            orden.fotos.isEmpty() ->
                Resultado.Error("Agrega al menos una foto del automovil")
            orden.fotos.size > MAX_FOTOS ->
                Resultado.Error("Puedes adjuntar como maximo $MAX_FOTOS fotos")
            else -> repository.registrar(orden.copy(falla = falla))
        }
    }

    companion object {
        const val MAX_FOTOS = 3
        const val MIN_CARACTERES_FALLA = 10
    }
}

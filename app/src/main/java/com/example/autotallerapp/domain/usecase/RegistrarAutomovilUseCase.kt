package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.PlacaValidador
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.Automovil
import com.example.autotallerapp.domain.repository.AutomovilRepository
import javax.inject.Inject

class RegistrarAutomovilUseCase @Inject constructor (
    private val repository: AutomovilRepository
) {
    suspend operator fun invoke(
        clienteId: String,
        placa: String,
        marca: String,
        modelo: String,
        anio: Int?,
        color: String
    ): Resultado<Automovil> {
        if (clienteId.isBlank()) {
            return Resultado.Error("Selecciona un cliente para el automovil")
        }
        if (!PlacaValidador.esValida(placa)) {
            return Resultado.Error("La placa no tiene un formato valido")
        }
        if (marca.isBlank()) {
            return Resultado.Error("Selecciona la marca del automovil")
        }
        if (modelo.isBlank()) {
            return Resultado.Error("Selecciona el modelo del automovil")
        }

        val placaFormateada = PlacaValidador.formatear(placa)
        if (repository.existePlaca(placaFormateada)) {
            return Resultado.Error("Ya existe un automovil registrado con esa placa")
        }

        val automovil = Automovil(
            clienteId = clienteId,
            placa = placaFormateada,
            marca = marca,
            modelo = modelo,
            anio = anio,
            color = color
        )
        return repository.registrar(automovil)
    }
}
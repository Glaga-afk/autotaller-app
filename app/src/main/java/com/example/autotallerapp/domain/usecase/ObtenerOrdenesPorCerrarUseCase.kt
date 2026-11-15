package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

/** HU08: solo se pueden cerrar las OST que estan en reparacion (ya tienen tecnicos, HU06). */
class ObtenerOrdenesPorCerrarUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    suspend operator fun invoke(): Resultado<List<OrdenServicio>> =
        repository.obtenerPorEstado(EstadoOrden.EN_REPARACION)
}

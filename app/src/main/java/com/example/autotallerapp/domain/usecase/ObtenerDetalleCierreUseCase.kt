package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.DetalleCierre
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

/** HU08: consulta la OST y los tecnicos que trabajaron en ella. */
class ObtenerDetalleCierreUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    suspend operator fun invoke(idOrden: String): Resultado<DetalleCierre> {
        val orden = when (val resultado = repository.obtenerPorId(idOrden)) {
            is Resultado.Exito -> resultado.dato
            is Resultado.Error -> return resultado
        }
        val trabajos = when (val resultado = repository.obtenerTrabajosAsignados(idOrden)) {
            is Resultado.Exito -> resultado.dato
            is Resultado.Error -> return resultado
        }
        return Resultado.Exito(DetalleCierre(orden, trabajos))
    }
}

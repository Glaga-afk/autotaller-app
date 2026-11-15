package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.ValidacionInforme
import com.example.autotallerapp.domain.repository.BitacoraRepository
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

/**
 * HU08: valida el informe, cierra la OST (estado ATENDIDA) y, si el problema
 * no existia, lo deja registrado en la bitacora.
 */
class CerrarOrdenUseCase @Inject constructor(
    private val ordenRepository: OrdenRepository,
    private val bitacoraRepository: BitacoraRepository,
    private val buscarSimilares: BuscarProblemasSimilaresUseCase
) {
    suspend operator fun invoke(informe: InformeTecnico): Resultado<Boolean> {
        val limpio = informe.copy(
            solucionAplicada = informe.solucionAplicada.trim(),
            trabajos = informe.trabajos.map { it.copy(trabajoRealizado = it.trabajoRealizado.trim()) },
            problemaNuevo = informe.problemaNuevo?.let {
                it.copy(titulo = it.titulo.trim(), descripcion = it.descripcion.trim())
            }
        )

        // "Si no existia": se compara contra la bitacora antes de registrar
        val existentes = limpio.problemaNuevo?.let { buscarSimilares(it.titulo) }.orEmpty()
        ValidacionInforme.validar(limpio, existentes)?.let { return Resultado.Error(it) }

        return when (val resultado = ordenRepository.cerrarConInforme(limpio)) {
            is Resultado.Exito -> {
                // El problema ya esta en Firestore; se copia a Room para que la busqueda lo vea al instante
                resultado.dato?.let { bitacoraRepository.guardarLocal(it) }
                Resultado.Exito(resultado.dato != null)
            }
            is Resultado.Error -> resultado
        }
    }
}

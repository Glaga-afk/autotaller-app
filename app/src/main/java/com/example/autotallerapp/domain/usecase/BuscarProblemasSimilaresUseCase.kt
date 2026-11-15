package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.ValidacionInforme
import com.example.autotallerapp.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * HU08: busca en la copia local de la bitacora (Room, HU04) problemas con
 * palabras parecidas al titulo, para no registrar uno que ya existe.
 */
class BuscarProblemasSimilaresUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(titulo: String): List<ProblemaTecnico> {
        val claves = ValidacionInforme.palabrasClave(titulo)
        if (claves.isBlank()) return emptyList()
        return try {
            repository.buscar(FiltroBitacora(palabraClave = claves)).first()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

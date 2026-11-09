package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.first
import java.text.Normalizer
import javax.inject.Inject

/**
 * Contrasta la sugerencia de la IA con la bitacora local (Room, HU04).
 * Toma los casos del mismo sistema y los ordena por cuantas palabras de la
 * sugerencia aparecen en cada caso. Funciona sin conexion.
 */

class ContrastarConBitacoraUseCase @Inject constructor(
    private val bitacora: BitacoraRepository
) {
    suspend operator fun invoke(
        sistema: SistemaAutomovil,
        causaProbable: String,
        palabrasClave: List<String>,
        marca: String,
        modelo: String
    ): List<ProblemaTecnico> {
        val casos = bitacora.buscar(FiltroBitacora(sistema = sistema)).first()

        // Raiz de 4 letras: "frenos", "frenar" y "frenado" comparten "fren"
        val raices = (palabrasClave + causaProbable.split(" ", ",", ".", ";"))
            .map { normalizar(it) }
            .filter { it.length >= LARGO_MINIMO }
            .map { it.take(LARGO_RAIZ) }
            .toSet()
        if (raices.isEmpty()) return emptyList()

        return casos
            .map { caso -> caso to puntaje(caso, raices, marca, modelo) }
            .filter { (_, puntos) -> puntos > 0 }
            .sortedByDescending { (_, puntos) -> puntos }
            .take(MAX_CASOS)
            .map { (caso, _) -> caso }
    }

    private fun puntaje(
        caso: ProblemaTecnico,
        raices: Set<String>,
        marca: String,
        modelo: String
    ): Int {
        val texto = normalizar("${caso.titulo} ${caso.descripcion} ${caso.solucion}")
        val coincidencias = raices.count { texto.contains(it) }
        if (coincidencias == 0) return 0

        var puntos = coincidencias * 2
        if (marca.isNotBlank() && caso.marca.equals(marca, ignoreCase = true)) puntos++
        if (modelo.isNotBlank() && caso.modelo.equals(modelo, ignoreCase = true)) puntos++
        return puntos
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(MARCAS_ACENTO, "")

    private companion object {
        const val LARGO_MINIMO = 4
        const val LARGO_RAIZ = 4
        const val MAX_CASOS = 3
        val MARCAS_ACENTO = Regex("\\p{Mn}+")
    }
}
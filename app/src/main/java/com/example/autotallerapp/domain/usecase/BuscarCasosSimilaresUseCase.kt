package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.first
import java.text.Normalizer
import javax.inject.Inject

/**
 * Busca en la bitacora local (Room, HU04) los casos que se parecen a un texto.
 * Compara raices de 4 letras ("frenos" y "frenar" comparten "fren") y da un
 * punto extra si coincide la marca o el modelo. Funciona sin conexion.
 */
class BuscarCasosSimilaresUseCase @Inject constructor(
    private val bitacora: BitacoraRepository
) {
    suspend operator fun invoke(
        consulta: String,
        marca: String = "",
        modelo: String = "",
        sistema: SistemaAutomovil? = null
    ): List<ProblemaTecnico> {
        val casos = bitacora.buscar(FiltroBitacora(sistema = sistema)).first()
        if (casos.isEmpty()) return emptyList()

        val raices = consulta.split(SEPARADORES)
            .map { normalizar(it) }
            .filter { it.length >= LARGO_MINIMO && it !in PALABRAS_VACIAS }
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
        val SEPARADORES = Regex("[^\\p{L}\\p{N}]+")
        val MARCAS_ACENTO = Regex("\\p{Mn}+")

        /** Palabras muy comunes que no ayudan a encontrar casos parecidos. */
        val PALABRAS_VACIAS = setOf(
            "para", "como", "pero", "porque", "cuando", "donde", "tiene", "tengo", "hacer",
            "esta", "estan", "este", "esto", "estos", "puede", "pueden", "debo", "debe",
            "cual", "cuales", "sobre", "entre", "desde", "hasta", "tambien", "solo", "hace",
            "hacen", "cuanto", "quiero", "necesito", "favor", "ayuda", "ayudame", "auto",
            "carro", "vehiculo", "falla", "problema", "revisar", "reviso", "causa", "causas"
        )
    }
}
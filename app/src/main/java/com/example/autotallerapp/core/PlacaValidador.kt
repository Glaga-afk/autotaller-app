package com.example.autotallerapp.core

/** Linea de texto leida por OCR con su tamano en pixeles (area de su caja) */
data class LineaOcr(val texto: String, val tamano: Int)

object PlacaValidador {
    // 3 caracteres alfanumericos (con al menos una letra) + 3 digitos: ABC-123 o A1B-234
    private val FORMATO_ESTANDAR = Regex("^[A-Z0-9]{3}[0-9]{3}$")
    // 2 letras + 4 digitos: AB-1234
    private val FORMATO_ANTIGUO = Regex("^[A-Z]{2}[0-9]{4}$")
    private val NO_ALFANUMERICO = Regex("[^A-Z0-9]")
    private val ESPACIOS = Regex("\\s+")

    private val NUMERO_A_LETRA = mapOf('0' to 'O', '1' to 'I', '8' to 'B', '5' to 'S', '2' to 'Z', '6' to 'G')
    private val LETRA_A_NUMERO = mapOf(
        'O' to '0', 'Q' to '0', 'D' to '0', 'I' to '1', 'L' to '1',
        'B' to '8', 'S' to '5', 'Z' to '2', 'G' to '6'
    )

    private data class Candidato(val texto: String, val tamano: Int)
    private data class Lectura(val placa: String, val tamano: Int, val exacta: Boolean)

    /** Deja solo letras y numeros en mayuscula (quita guiones, espacios, puntos, etc.) */
    fun normalizar(texto: String): String =
        texto.uppercase().replace(NO_ALFANUMERICO, "")

    fun esValida(placa: String): Boolean {
        val limpia = normalizar(placa)
        return FORMATO_ANTIGUO.matches(limpia) ||
                (FORMATO_ESTANDAR.matches(limpia) && limpia.take(3).any { it.isLetter() })
    }

    fun formatear(placa: String): String {
        val limpia = normalizar(placa)
        return when {
            FORMATO_ANTIGUO.matches(limpia) -> "${limpia.take(2)}-${limpia.substring(2)}"
            esValida(limpia) -> "${limpia.take(3)}-${limpia.substring(3)}"
            else -> limpia
        }
    }

    /**
     * Entre las lineas que estan dentro del recuadro, devuelve la placa valida de mayor tamano.
     * Si empatan en tamano, prefiere la que se leyo exacta (sin correcciones O/0, B/8...).
     */
    fun buscarMasGrande(lineas: List<LineaOcr>): String? =
        generarCandidatos(lineas)
            .mapNotNull { candidato ->
                val exacta = candidato.texto.takeIf { esValida(it) }
                val placa = exacta ?: corregir(candidato.texto)
                placa?.let { Lectura(it, candidato.tamano, exacta != null) }
            }
            .maxWithOrNull(compareBy<Lectura>({ it.tamano }, { it.exacta }))
            ?.let { formatear(it.placa) }

    private fun generarCandidatos(lineas: List<LineaOcr>): List<Candidato> {
        val candidatos = mutableListOf<Candidato>()

        lineas.forEach { linea ->
            val palabras = linea.texto.trim().split(ESPACIOS)
                .map { normalizar(it) }
                .filter { it.isNotEmpty() }

            candidatos += Candidato(normalizar(linea.texto), linea.tamano)
            palabras.forEach { candidatos += Candidato(it, linea.tamano) }
            for (i in 0 until palabras.size - 1) {
                candidatos += Candidato(palabras[i] + palabras[i + 1], linea.tamano)
            }
        }

        for (i in 0 until lineas.size - 1) {
            val a = lineas[i]
            val b = lineas[i + 1]
            candidatos += Candidato(
                normalizar(a.texto) + normalizar(b.texto),
                maxOf(a.tamano, b.tamano)
            )
        }

        return candidatos.filter { it.texto.length == 6 }
    }

    private fun corregir(candidato: String): String? {
        val estandar = candidato.take(3) +
                candidato.drop(3).map { LETRA_A_NUMERO[it] ?: it }.joinToString("")
        if (esValida(estandar)) return estandar

        val antiguo = candidato.take(2).map { NUMERO_A_LETRA[it] ?: it }.joinToString("") +
                candidato.drop(2).map { LETRA_A_NUMERO[it] ?: it }.joinToString("")
        return antiguo.takeIf { esValida(it) }
    }
}

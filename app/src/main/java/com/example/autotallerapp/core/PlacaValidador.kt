package com.example.autotallerapp.core

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
     * Busca una placa entre las lineas leidas por OCR.
     * Solo considera textos de exactamente 6 caracteres (linea completa, cada palabra,
     * o dos partes contiguas como "ABC" + "123"). Primero exacto; si no hay,
     * corrige confusiones tipicas (O/0, B/8, S/5...).
     */
    fun buscarEnLineas(lineas: List<String>): String? {
        val candidatos = generarCandidatos(lineas)
        candidatos.firstOrNull { esValida(it) }?.let { return formatear(it) }
        candidatos.firstNotNullOfOrNull { corregir(it) }?.let { return formatear(it) }
        return null
    }

    private fun generarCandidatos(lineas: List<String>): List<String> {
        val candidatos = mutableListOf<String>()
        lineas.forEach { linea ->
            val palabras = linea.trim().split(ESPACIOS).map { normalizar(it) }.filter { it.isNotEmpty() }
            candidatos += normalizar(linea)
            candidatos += palabras
            candidatos += palabras.zipWithNext { a, b -> a + b }
        }
        candidatos += lineas.map { normalizar(it) }.zipWithNext { a, b -> a + b }
        return candidatos.filter { it.length == 6 }.distinct()
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

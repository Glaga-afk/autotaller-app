package com.example.autotallerapp.domain.model

import java.text.Normalizer
import java.util.Locale

/**
 * Reglas del informe tecnico (HU08). No depende de Android, por eso
 * se prueba con JUnit (ValidacionInformeTest).
 */
object ValidacionInforme {

    const val MINIMO_SOLUCION = 10

    /** Devuelve el mensaje del primer error encontrado, o null si el informe es valido. */
    fun validar(
        informe: InformeTecnico,
        problemasExistentes: List<ProblemaTecnico> = emptyList()
    ): String? {
        if (informe.solucionAplicada.trim().length < MINIMO_SOLUCION) {
            return "Describe la solucion aplicada (minimo $MINIMO_SOLUCION caracteres)"
        }
        if (informe.trabajos.isEmpty()) {
            return "La OST no tiene tecnicos asignados. Asignalos primero (HU06)"
        }
        informe.trabajos.firstOrNull { it.trabajoRealizado.isBlank() }?.let {
            return "Indica el trabajo que realizo ${it.nombreTecnico}"
        }
        informe.problemaNuevo?.let { problema ->
            if (problema.titulo.isBlank()) return "Escribe el titulo del problema nuevo"
            if (problema.descripcion.isBlank()) return "Escribe la causa del problema nuevo"
            if (esDuplicado(problema, problemasExistentes)) {
                return "Ese problema ya existe en la bitacora: \"${problema.titulo.trim()}\""
            }
        }
        return null
    }

    /**
     * "Si no existia": se considera repetido cuando ya hay un problema del mismo
     * sistema con el mismo titulo (sin importar mayusculas, tildes ni espacios).
     */
    fun esDuplicado(nuevo: ProblemaTecnico, existentes: List<ProblemaTecnico>): Boolean {
        val titulo = normalizar(nuevo.titulo)
        return existentes.any { it.sistema == nuevo.sistema && normalizar(it.titulo) == titulo }
    }

    fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()

    /**
     * Palabras para buscar coincidencias en la bitacora (busqueda FTS de Room).
     * Se quitan signos (rompen la consulta FTS) y palabras de menos de 3 letras.
     */
    fun palabrasClave(texto: String): String =
        texto.split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 3 }
            .take(5)
            .joinToString(" ")
}

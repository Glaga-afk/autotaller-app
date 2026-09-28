package com.example.autotallerapp.ui.ocr

class EstabilizadorPlaca(
    private val ventanaMs: Long = 2000L,
    private val lecturasMinimas: Int = 3,
    private val toleranciaSinLecturaMs: Long = 800L
) {
    data class Estado(
        val candidata: String? = null,
        val progreso: Float = 0f,
        val confirmada: Boolean = false
    )

    private val votos = mutableMapOf<String, Int>()
    private var inicioMs = 0L
    private var ultimaLecturaMs = 0L

    @Synchronized
    fun registrar(placa: String, ahoraMs: Long) {
        if (votos.isEmpty()) inicioMs = ahoraMs
        votos[placa] = (votos[placa] ?: 0) + 1
        ultimaLecturaMs = ahoraMs
    }

    @Synchronized
    fun evaluar(ahoraMs: Long): Estado {
        if (votos.isEmpty()) return Estado()

        // Si dejo de leer la placa (camara movida), se reinicia el conteo
        if (ahoraMs - ultimaLecturaMs > toleranciaSinLecturaMs) {
            votos.clear()
            return Estado()
        }

        val lider = votos.maxByOrNull { it.value }!!
        val totalVotos = votos.values.sum()
        val progreso = ((ahoraMs - inicioMs) / ventanaMs.toFloat()).coerceIn(0f, 1f)
        val confirmada = progreso >= 1f &&
                lider.value >= lecturasMinimas &&
                lider.value * 100 >= totalVotos * 60 // el ganador debe tener al menos 60% de los votos

        return Estado(candidata = lider.key, progreso = progreso, confirmada = confirmada)
    }

    @Synchronized
    fun reiniciar() = votos.clear()
}
package com.example.autotallerapp.domain.model

/**
 * Ciclo de vida de la Orden de Servicio Tecnico.
 * La HU05 solo crea ordenes en PENDIENTE; los demas estados los usan
 * la asignacion de tecnicos (HU06), la bandeja (HU07) y el cierre (HU08).
 */
enum class EstadoOrden(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_DIAGNOSTICO("En diagnostico"),
    EN_REPARACION("En reparacion"),
    ATENDIDA("Atendida");

    companion object {
        fun desde(valor: String?): EstadoOrden =
            entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

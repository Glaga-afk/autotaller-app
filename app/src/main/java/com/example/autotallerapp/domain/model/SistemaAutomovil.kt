package com.example.autotallerapp.domain.model

enum class SistemaAutomovil (val etiqueta: String) {
    MOTOR("Motor"),
    FRENOS("Frenos"),
    TRANSMISION("Transmision"),
    ELECTRICO("Sistema electrico"),
    SUSPENSION("Suspension"),
    REFRIGERACION("Refrigeracion"),
    DIRECCION("Direccion"),
    OTRO("Otro");

    companion object {
        fun desde(valor: String?): SistemaAutomovil =
            entries.firstOrNull { it.name == valor?.uppercase() } ?: OTRO
    }
}
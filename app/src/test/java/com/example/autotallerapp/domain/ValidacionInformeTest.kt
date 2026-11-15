package com.example.autotallerapp.domain

import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.example.autotallerapp.domain.model.ValidacionInforme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas del informe tecnico (HU08). */
class ValidacionInformeTest {

    private val trabajoCompleto = TrabajoTecnico(
        tecnicoId = "t1",
        nombreTecnico = "Carlos Ruiz",
        tareaAsignada = "Revisar frenos",
        trabajoRealizado = "Cambio de pastillas delanteras"
    )

    private val informeValido = InformeTecnico(
        idOrden = "orden-1",
        solucionAplicada = "Se cambiaron las pastillas y se rectificaron los discos",
        trabajos = listOf(trabajoCompleto)
    )

    private fun problema(titulo: String, sistema: SistemaAutomovil = SistemaAutomovil.FRENOS) =
        ProblemaTecnico(
            id = "",
            sistema = sistema,
            marca = "Toyota",
            modelo = "Yaris",
            titulo = titulo,
            descripcion = "Desgaste por uso",
            solucion = "Cambio de pastillas",
            actualizadoEn = 0L
        )

    @Test
    fun informeCompleto_esValido() {
        assertNull(ValidacionInforme.validar(informeValido))
    }

    @Test
    fun solucionCorta_esRechazada() {
        val informe = informeValido.copy(solucionAplicada = "Listo")
        assertNotNull(ValidacionInforme.validar(informe))
    }

    @Test
    fun sinTecnicos_esRechazado() {
        val informe = informeValido.copy(trabajos = emptyList())
        assertNotNull(ValidacionInforme.validar(informe))
    }

    @Test
    fun tecnicoSinTrabajoRealizado_esRechazadoConSuNombre() {
        val informe = informeValido.copy(
            trabajos = listOf(trabajoCompleto, trabajoCompleto.copy(tecnicoId = "t2", nombreTecnico = "Ana Diaz", trabajoRealizado = " "))
        )
        assertEquals("Indica el trabajo que realizo Ana Diaz", ValidacionInforme.validar(informe))
    }

    @Test
    fun problemaNuevoSinCausa_esRechazado() {
        val informe = informeValido.copy(problemaNuevo = problema("Chirrido al frenar").copy(descripcion = ""))
        assertNotNull(ValidacionInforme.validar(informe))
    }

    @Test
    fun problemaQueYaExiste_noSeRegistraDeNuevo() {
        val existentes = listOf(problema("Chirrido al frenar"))
        val informe = informeValido.copy(problemaNuevo = problema("  CHIRRIDO   al frenár "))
        assertNotNull(ValidacionInforme.validar(informe, existentes))
    }

    @Test
    fun problemaParecidoDeOtroSistema_noEsDuplicado() {
        val existentes = listOf(problema("Chirrido al frenar", SistemaAutomovil.SUSPENSION))
        assertFalse(ValidacionInforme.esDuplicado(problema("Chirrido al frenar"), existentes))
    }

    @Test
    fun problemaNuevoDistinto_esValido() {
        val existentes = listOf(problema("Pedal de freno esponjoso"))
        val informe = informeValido.copy(problemaNuevo = problema("Chirrido al frenar"))
        assertNull(ValidacionInforme.validar(informe, existentes))
    }

    @Test
    fun palabrasClave_quitaSignosYPalabrasCortas() {
        assertEquals("Ruido freno delantero", ValidacionInforme.palabrasClave("Ruido \"en\" freno-delantero!"))
    }

    @Test
    fun normalizar_quitaTildesYEspacios() {
        assertTrue(ValidacionInforme.normalizar("  Dirección   DURA ") == "direccion dura")
    }
}

package com.example.autotallerapp.domain

import com.example.autotallerapp.domain.model.MensajeAvisoCliente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del aviso por WhatsApp/SMS al cliente (HU08). */
class MensajeAvisoClienteTest {

    @Test
    fun celularDeNueveDigitos_agregaCodigoDePeru() {
        assertEquals("51987654321", MensajeAvisoCliente.normalizarTelefono("987 654 321"))
    }

    @Test
    fun celularConMasCincuentaYUno_seRespeta() {
        assertEquals("51987654321", MensajeAvisoCliente.normalizarTelefono("+51 987-654-321"))
    }

    @Test
    fun telefonoFijoOIncompleto_noSePuedeAvisar() {
        assertNull(MensajeAvisoCliente.normalizarTelefono("014567890"))
        assertNull(MensajeAvisoCliente.normalizarTelefono("98765"))
        assertNull(MensajeAvisoCliente.normalizarTelefono(""))
    }

    @Test
    fun mensaje_incluyeVehiculoOrdenYTrabajo() {
        val mensaje = MensajeAvisoCliente.armar(
            nombreCliente = "Juan Perez",
            marca = "Toyota",
            modelo = "Yaris",
            placa = "ABC-123",
            numeroOrden = "OST-000027",
            trabajoRealizado = "Cambio de pastillas de freno"
        )
        assertTrue(mensaje.startsWith("Hola Juan Perez"))
        assertTrue(mensaje.contains("Toyota Yaris de placa ABC-123 (orden OST-000027)"))
        assertTrue(mensaje.contains("listo para ser recogido"))
        assertTrue(mensaje.contains("Trabajo realizado: Cambio de pastillas de freno"))
    }

    @Test
    fun ordenSinNumero_noMuestraParentesis() {
        val mensaje = MensajeAvisoCliente.armar("Ana", "Kia", "Rio", "XYZ-987", null, "")
        assertFalse(mensaje.contains("(orden"))
        assertFalse(mensaje.contains("Trabajo realizado"))
    }
}

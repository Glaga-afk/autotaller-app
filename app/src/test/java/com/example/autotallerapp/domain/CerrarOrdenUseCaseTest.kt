package com.example.autotallerapp.domain

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.FiltroBitacora
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.example.autotallerapp.domain.repository.BitacoraRepository
import com.example.autotallerapp.domain.repository.OrdenRepository
import com.example.autotallerapp.domain.usecase.BuscarProblemasSimilaresUseCase
import com.example.autotallerapp.domain.usecase.CerrarOrdenUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del caso de uso que cierra la OST (HU08), con repositorios falsos. */
class CerrarOrdenUseCaseTest {

    private class OrdenRepositoryFalso : OrdenRepository {
        var informeRecibido: InformeTecnico? = null

        override suspend fun cerrarConInforme(informe: InformeTecnico): Resultado<ProblemaTecnico?> {
            informeRecibido = informe
            return Resultado.Exito(informe.problemaNuevo?.copy(id = "nuevo-1"))
        }

        override suspend fun registrar(orden: OrdenServicio) = Resultado.Exito(orden)
        override fun observar(idLocal: String): Flow<OrdenServicio?> = flowOf(null)
        override suspend fun sincronizarPendientes() = Resultado.Exito(0)
        override suspend fun obtenerTodas() = Resultado.Exito(emptyList<OrdenServicio>())
        override suspend fun obtenerLocales() = Resultado.Exito(emptyList<OrdenServicio>())
        override suspend fun obtenerPorEstado(estado: EstadoOrden) = Resultado.Exito(emptyList<OrdenServicio>())
        override suspend fun obtenerPorId(idOrden: String) = Resultado.Exito(OrdenServicio(idLocal = idOrden))
        override suspend fun obtenerTrabajosAsignados(idOrden: String) = Resultado.Exito(emptyList<TrabajoTecnico>())
        override suspend fun registrarAvisoCliente(idOrden: String, canal: CanalAviso) = Resultado.Exito(Unit)
    }

    private class BitacoraRepositoryFalsa(private val existentes: List<ProblemaTecnico>) : BitacoraRepository {
        val guardadosLocal = mutableListOf<ProblemaTecnico>()

        override fun buscar(filtro: FiltroBitacora): Flow<List<ProblemaTecnico>> = flowOf(existentes)
        override suspend fun sincronizar() = Resultado.Exito(0)
        override suspend fun hayDatosLocales() = existentes.isNotEmpty()
        override suspend fun guardarLocal(problema: ProblemaTecnico) {
            guardadosLocal.add(problema)
        }
    }

    private fun problema(titulo: String) = ProblemaTecnico(
        id = "", sistema = SistemaAutomovil.FRENOS, marca = "Toyota", modelo = "Yaris",
        titulo = titulo, descripcion = "Pastillas cristalizadas", solucion = "", actualizadoEn = 0L
    )

    private val informe = InformeTecnico(
        idOrden = "orden-1",
        solucionAplicada = "  Cambio de pastillas y rectificado de discos  ",
        trabajos = listOf(TrabajoTecnico("t1", "Carlos Ruiz", "Frenos", " Cambio de pastillas "))
    )

    private fun casoDeUso(ordenes: OrdenRepository, bitacora: BitacoraRepository) =
        CerrarOrdenUseCase(ordenes, bitacora, BuscarProblemasSimilaresUseCase(bitacora))

    @Test
    fun cierraLaOrdenConLosTextosLimpios() = runBlocking {
        val ordenes = OrdenRepositoryFalso()
        val resultado = casoDeUso(ordenes, BitacoraRepositoryFalsa(emptyList()))(informe)

        assertTrue(resultado is Resultado.Exito)
        assertEquals("Cambio de pastillas y rectificado de discos", ordenes.informeRecibido?.solucionAplicada)
        assertEquals("Cambio de pastillas", ordenes.informeRecibido?.trabajos?.first()?.trabajoRealizado)
    }

    @Test
    fun problemaNuevo_seGuardaTambienEnRoom() = runBlocking {
        val bitacora = BitacoraRepositoryFalsa(emptyList())
        val resultado = casoDeUso(OrdenRepositoryFalso(), bitacora)(
            informe.copy(problemaNuevo = problema("Chirrido al frenar"))
        )

        assertEquals(Resultado.Exito(true), resultado)
        assertEquals("nuevo-1", bitacora.guardadosLocal.single().id)
    }

    @Test
    fun problemaExistente_noCierraLaOrden() = runBlocking {
        val ordenes = OrdenRepositoryFalso()
        val bitacora = BitacoraRepositoryFalsa(listOf(problema("Chirrido al frenar")))
        val resultado = casoDeUso(ordenes, bitacora)(
            informe.copy(problemaNuevo = problema("chirrido al frenar"))
        )

        assertTrue(resultado is Resultado.Error)
        assertNull(ordenes.informeRecibido)
        assertTrue(bitacora.guardadosLocal.isEmpty())
    }
}

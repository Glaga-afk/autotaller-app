package com.example.autotallerapp.ui.screens.indicadores

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.domain.model.InformeTecnico
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.repository.OrdenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeOrdenRepository(private val resultado: Resultado<List<OrdenServicio>>) : OrdenRepository {
    override suspend fun registrar(orden: OrdenServicio): Resultado<OrdenServicio> = Resultado.Exito(orden)
    override fun observar(idLocal: String): Flow<OrdenServicio?> = flowOf(null)
    override suspend fun sincronizarPendientes(): Resultado<Int> = Resultado.Exito(0)
    override suspend fun obtenerTodas(): Resultado<List<OrdenServicio>> = resultado
    override suspend fun obtenerLocales(): Resultado<List<OrdenServicio>> = resultado

    // HU08: metodos del cierre de la OST (no se usan en estas pruebas)
    override suspend fun obtenerPorEstado(estado: EstadoOrden): Resultado<List<OrdenServicio>> = resultado
    override suspend fun obtenerPorId(idOrden: String): Resultado<OrdenServicio> = Resultado.Error("No usado")
    override suspend fun obtenerTrabajosAsignados(idOrden: String): Resultado<List<TrabajoTecnico>> = Resultado.Exito(emptyList())
    override suspend fun cerrarConInforme(informe: InformeTecnico): Resultado<ProblemaTecnico?> = Resultado.Exito(null)
    override suspend fun registrarAvisoCliente(idOrden: String, canal: CanalAviso): Resultado<Unit> = Resultado.Exito(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class IndicadoresViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cuando_se_cargan_indicadores_exitosamente_actualiza_estado() = runTest {
        val ordenesFalsa = listOf(
            OrdenServicio(
                idLocal = "1",
                numero = 1,
                estado = EstadoOrden.PENDIENTE,
                falla = "Frenos desgastados",
                fechaRegistro = System.currentTimeMillis()
            ),
            OrdenServicio(
                idLocal = "2",
                numero = 2,
                estado = EstadoOrden.ATENDIDA,
                falla = "Frenos desgastados",
                fechaRegistro = System.currentTimeMillis()
            )
        )

        val fakeRepo = FakeOrdenRepository(Resultado.Exito(ordenesFalsa))

        val viewModel = IndicadoresViewModel(null, fakeRepo)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals(2, estado.totalOrdenes)
        assertEquals(1, estado.ordenesPorEstado[EstadoOrden.PENDIENTE])
        assertEquals(1, estado.ordenesPorEstado[EstadoOrden.ATENDIDA])
        assertEquals("Frenos desgastados", estado.fallasFrecuentes.first().first)
    }
}

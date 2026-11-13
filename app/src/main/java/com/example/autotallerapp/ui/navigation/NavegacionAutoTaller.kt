package com.example.autotallerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autotallerapp.ui.screens.inicio.InicioScreen
import com.example.autotallerapp.ui.screens.login.LoginScreen
import com.example.autotallerapp.ui.screens.pendiente.PendienteScreen
import com.example.autotallerapp.ui.screens.placeholder.ModuloPendienteScreen
import com.example.autotallerapp.ui.screens.recuperar.RecuperarScreen
import com.example.autotallerapp.ui.screens.registro.RegistroScreen
import com.example.autotallerapp.ui.screens.splash.SplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.autotallerapp.ui.screens.automovil.RegistroAutomovilScreen
import com.example.autotallerapp.ui.screens.escaner.EscanerPlacaScreen
import com.example.autotallerapp.ui.screens.automovil.ListaAutomovilesScreen
import com.example.autotallerapp.ui.screens.bitacora.BitacoraScreen
import com.example.autotallerapp.ui.cliente.ClienteScreen
import com.example.autotallerapp.ui.screens.asistente.AsistenteIaScreen
import com.example.autotallerapp.ui.screens.orden.CapturaFotoScreen
import com.example.autotallerapp.ui.screens.orden.NuevaOrdenScreen
import com.example.autotallerapp.ui.screens.orden.NuevaOstClienteScreen
import com.example.autotallerapp.ui.screens.orden.ListaOrdenesScreen
import com.example.autotallerapp.ui.screens.orden.AsignarTecnicoScreen
import com.example.autotallerapp.ui.screens.tecnico.ListaTecnicosScreen
import com.example.autotallerapp.ui.screens.diagnostico.DiagnosticoIAScreen
import com.example.autotallerapp.ui.screens.diagnostico.SeleccionOrdenScreen

/** Clave con la que la camara devuelve la ruta de la foto a la pantalla de la orden. */
private const val CLAVE_FOTO_CAPTURADA = "foto_capturada"

@Composable
fun NavegacionAutoTaller(navController: NavHostController = rememberNavController()) {

    NavHost(navController = navController, startDestination = Rutas.SPLASH) {

        composable(Rutas.SPLASH) {
            SplashScreen(
                irALogin = { navController.reemplazarPor(Rutas.LOGIN) },
                irAInicio = { navController.reemplazarPor(Rutas.INICIO) },
                irAPendiente = { navController.reemplazarPor(Rutas.PENDIENTE) }
            )
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                irARegistro = { navController.navigate(Rutas.REGISTRO) },
                irARecuperar = { navController.navigate(Rutas.RECUPERAR) },
                irAInicio = { navController.reemplazarPor(Rutas.INICIO) },
                irAPendiente = { navController.reemplazarPor(Rutas.PENDIENTE) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                volver = { navController.popBackStack() },
                irAPendiente = { navController.reemplazarPor(Rutas.PENDIENTE) }
            )
        }

        composable(Rutas.RECUPERAR) {
            RecuperarScreen(volver = { navController.popBackStack() })
        }

        composable(Rutas.ORDENES) {
            ListaOrdenesScreen(
                volver = { navController.popBackStack() },
                irADetalleOrden = { ordenId ->
                    // 👈 Navegamos a la pantalla de asignación de técnicos de la orden (HU06)
                    navController.navigate(Rutas.rutaAsignarTecnico(ordenId))
                }
            )
        }

        // Ruta para el listado general y gestión de técnicos (HU06)
        composable(Rutas.TECNICOS) {
            ListaTecnicosScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        // 👈 Ruta real para la Gestión y Asignación de Técnicos (HU06)
        composable(
            route = Rutas.ASIGNAR_TECNICO,
            arguments = listOf(navArgument("ordenId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getString("ordenId").orEmpty()
            AsignarTecnicoScreen(
                idOrdenLocal = ordenId,
                onVolver = { navController.popBackStack() },
                onAsignacionExitosa = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.PENDIENTE) {
            PendienteScreen(
                irALogin = { navController.reemplazarPor(Rutas.LOGIN) }
            )
        }

        composable(Rutas.INICIO) {
            InicioScreen(
                irALogin = { navController.reemplazarPor(Rutas.LOGIN) },
                irAModulo = { ruta -> navController.navigate(ruta) }
            )
        }

        composable(Rutas.BITACORA) {
            BitacoraScreen(
                volver = { navController.popBackStack() }
            )
        }

        // Ruta real para la Gestión de Clientes (HU02)
        composable(Rutas.CLIENTES) {
            ClienteScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onClienteConfirmado = { dniCliente ->
                    navController.navigate(Rutas.rutaAutomovilesCliente(dniCliente))
                }
            )
        }

        //Automovil
        composable(
            route = Rutas.NUEVO_AUTOMOVIL,
            arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
        ) { entrada ->
            val clienteId = entrada.arguments?.getString("clienteId").orEmpty()
            val placaEscaneada by entrada.savedStateHandle
                .getStateFlow<String?>("placa_escaneada", null)
                .collectAsState()

            RegistroAutomovilScreen(
                clienteId = clienteId,
                placaEscaneada = placaEscaneada,
                volver = { navController.popBackStack() },
                irAEscanerPlaca = { navController.navigate(Rutas.ESCANER_PLACA) },
                alRegistrar = { navController.popBackStack() }
            )
        }

        composable(Rutas.ESCANER_PLACA) {
            EscanerPlacaScreen(
                volver = { navController.popBackStack() },
                alDetectarPlaca = { placa ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("placa_escaneada", placa)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Rutas.AUTOMOVILES_CLIENTE,
            arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
        ) { entrada ->
            val clienteId = entrada.arguments?.getString("clienteId").orEmpty()

            ListaAutomovilesScreen(
                volver = { navController.popBackStack() },
                irANuevoAutomovil = { navController.navigate(Rutas.rutaNuevoAutomovil(clienteId)) },
                alSeleccionarAutomovil = { automovil ->
                    navController.navigate(Rutas.rutaNuevaOrden(clienteId, automovil.id))
                }
            )
        }

        // Registro de la orden de servicio (HU05)
        composable(
            route = Rutas.NUEVA_ORDEN_AUTOMOVIL,
            arguments = listOf(
                navArgument("clienteId") { type = NavType.StringType },
                navArgument("automovilId") { type = NavType.StringType }
            )
        ) { entrada ->
            val fotoCapturada by entrada.savedStateHandle
                .getStateFlow<String?>(CLAVE_FOTO_CAPTURADA, null)
                .collectAsState()

            NuevaOrdenScreen(
                fotoCapturada = fotoCapturada,
                alConsumirFoto = { entrada.savedStateHandle[CLAVE_FOTO_CAPTURADA] = null },
                volver = { navController.popBackStack() },
                irACamara = { navController.navigate(Rutas.CAPTURA_FOTO) },
                alFinalizar = { navController.popBackStack(Rutas.INICIO, inclusive = false) }
            )
        }

        composable(Rutas.CAPTURA_FOTO) {
            CapturaFotoScreen(
                volver = { navController.popBackStack() },
                alCapturar = { ruta ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(CLAVE_FOTO_CAPTURADA, ruta)
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.NUEVA_OST_CLIENTE) {
            NuevaOstClienteScreen(
                onVolver = { navController.popBackStack() },
                onClienteSeleccionado = { dniCliente ->
                    navController.navigate(Rutas.rutaAutomovilesCliente(dniCliente))
                }
            )
        }

        // Diagnostico con IA (HU09)
        composable(Rutas.DIAGNOSTICO_IA) {
            SeleccionOrdenScreen(
                volver = { navController.popBackStack() },
                alSeleccionarOrden = { ordenId ->
                    navController.navigate(Rutas.rutaDiagnosticoOrden(ordenId))
                }
            )
        }

        composable(
            route = Rutas.DIAGNOSTICO_ORDEN,
            arguments = listOf(navArgument("ordenId") { type = NavType.StringType })
        ) {
            DiagnosticoIAScreen(volver = { navController.popBackStack() })
        }

        // Asistente técnico con IA por voz (HU12)
        composable(Rutas.ASISTENTE_IA) {
            AsistenteIaScreen(volver = { navController.popBackStack() })
        }

        modulosPendientes(navController)
    }
}

private fun NavHostController.reemplazarPor(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun androidx.navigation.NavGraphBuilder.modulosPendientes(nav: NavHostController) {
    val modulos = listOf(
        // Rutas.CLIENTES to "Clientes", // Movido a su pantalla real arriba
        Rutas.AUTOMOVILES to "Automoviles",
        Rutas.ORDENES to "Ordenes de servicio",
        // Rutas.TECNICOS to "Tecnicos",
        // Rutas.DIAGNOSTICO_IA to "Diagnostico con IA", // Movido a su pantalla real arriba
        // Rutas.ASISTENTE_IA to "Asistente tecnico",
        Rutas.USUARIOS to "Usuarios y roles",
        Rutas.INDICADORES to "Indicadores",
        Rutas.CATALOGOS to "Catalogos",
        Rutas.PERFIL to "Mi perfil"
    )
    modulos.forEach { (ruta, titulo) ->
        composable(ruta) {
            ModuloPendienteScreen(titulo = titulo, volver = { nav.popBackStack() })
        }
    }
}
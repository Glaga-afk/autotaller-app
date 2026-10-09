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
                irANuevoAutomovil = { navController.navigate(Rutas.rutaNuevoAutomovil(clienteId)) }
            )
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
        Rutas.CLIENTES to "Clientes",
        Rutas.AUTOMOVILES to "Automoviles",
        Rutas.ORDENES to "Ordenes de servicio",
        Rutas.NUEVA_ORDEN to "Nueva OST",
        // Rutas.BITACORA to "Bitacora de problemas"
        Rutas.TECNICOS to "Tecnicos",
        Rutas.DIAGNOSTICO_IA to "Diagnostico con IA",
        Rutas.ASISTENTE_IA to "Asistente tecnico",
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

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
        Rutas.BITACORA to "Bitacora de problemas",
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

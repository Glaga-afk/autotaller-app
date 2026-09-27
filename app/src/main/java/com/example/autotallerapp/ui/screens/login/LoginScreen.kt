package com.example.autotallerapp.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.autotallerapp.ui.auth.GoogleAuthCliente
import com.example.autotallerapp.ui.components.BotonPrincipal
import com.example.autotallerapp.ui.components.BotonSecundario
import com.example.autotallerapp.ui.components.CampoPassword
import com.example.autotallerapp.ui.components.CampoTexto
import com.example.autotallerapp.ui.components.LogoAutoTaller

@Composable
fun LoginScreen(
    irARegistro: () -> Unit,
    irARecuperar: () -> Unit,
    irAInicio: () -> Unit,
    irAPendiente: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val googleCliente = remember(contexto) { GoogleAuthCliente(contexto) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                LoginEvento.IrAInicio -> irAInicio()
                LoginEvento.IrAPendiente -> irAPendiente()
            }
        }
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            LogoAutoTaller(tamano = 56.dp)
            Spacer(Modifier.height(20.dp))
            Text("Iniciar sesion", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Accede con tu cuenta del taller",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            CampoTexto(
                valor = estado.correo,
                alCambiar = viewModel::alCambiarCorreo,
                etiqueta = "Correo",
                error = estado.errorCorreo,
                habilitado = estado.formularioHabilitado,
                tipoTeclado = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            CampoPassword(
                valor = estado.password,
                alCambiar = viewModel::alCambiarPassword,
                etiqueta = "Contrasena",
                error = estado.errorPassword,
                habilitado = estado.formularioHabilitado,
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = irARecuperar,
                    enabled = estado.formularioHabilitado
                ) { Text("Olvide mi contrasena") }
            }

            Spacer(Modifier.height(8.dp))

            BotonPrincipal(
                texto = "Iniciar sesion",
                alPulsar = viewModel::alPulsarIngresar,
                cargando = estado.cargando,
                habilitado = estado.formularioHabilitado
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "  o  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            BotonSecundario(
                texto = "Continuar con Google",
                habilitado = estado.formularioHabilitado,
                icono = {
                    if (estado.cargandoGoogle) {
                        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                    }
                },
                alPulsar = {
                    viewModel.alIniciarGoogle()
                    alcance.launch {
                        runCatching { googleCliente.obtenerIdToken() }
                            .onSuccess { viewModel.alObtenerIdTokenGoogle(it) }
                            .onFailure {
                                viewModel.alFallarGoogle(
                                    "No se pudo continuar con Google. Intentalo de nuevo"
                                )
                            }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = irARegistro,
                    enabled = estado.formularioHabilitado
                ) { Text("Crear cuenta") }
            }
        }
    }
}

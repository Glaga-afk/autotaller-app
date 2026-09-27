package com.example.autotallerapp.ui.screens.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.ui.components.BotonPrincipal
import com.example.autotallerapp.ui.components.CampoPassword
import com.example.autotallerapp.ui.components.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    volver: () -> Unit,
    irAPendiente: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cuentaCreada.collect { irAPendiente() }
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            CampoTexto(
                valor = estado.nombre,
                alCambiar = viewModel::alCambiarNombre,
                etiqueta = "Nombre completo",
                error = estado.errorNombre,
                habilitado = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = estado.correo,
                alCambiar = viewModel::alCambiarCorreo,
                etiqueta = "Correo",
                error = estado.errorCorreo,
                habilitado = !estado.cargando,
                tipoTeclado = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            CampoPassword(
                valor = estado.password,
                alCambiar = viewModel::alCambiarPassword,
                etiqueta = "Contrasena",
                error = estado.errorPassword,
                soporte = "Minimo 8 caracteres, una mayuscula y un numero",
                habilitado = !estado.cargando,
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )

            CampoPassword(
                valor = estado.confirmacion,
                alCambiar = viewModel::alCambiarConfirmacion,
                etiqueta = "Confirmar contrasena",
                error = estado.errorConfirmacion,
                habilitado = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tu rol lo asigna el administrador del taller. Podras ingresar cuando lo active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(24.dp))

            BotonPrincipal(
                texto = "Crear cuenta",
                alPulsar = viewModel::alPulsarCrearCuenta,
                cargando = estado.cargando
            )
        }
    }
}

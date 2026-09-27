package com.example.autotallerapp.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.ui.components.LogoAutoTaller

@Composable
fun SplashScreen(
    irALogin: () -> Unit,
    irAInicio: () -> Unit,
    irAPendiente: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destino by viewModel.destino.collectAsStateWithLifecycle()

    LaunchedEffect(destino) {
        when (destino) {
            DestinoInicial.Login -> irALogin()
            DestinoInicial.Inicio -> irAInicio()
            DestinoInicial.Pendiente -> irAPendiente()
            DestinoInicial.Cargando -> Unit
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoAutoTaller(tamano = 88.dp)
            Spacer(Modifier.height(20.dp))
            Text("AutoTaller", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Ordenes de servicio tecnico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Verificando tu sesion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

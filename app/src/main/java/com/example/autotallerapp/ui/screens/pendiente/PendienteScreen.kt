package com.example.autotallerapp.ui.screens.pendiente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autotallerapp.ui.screens.inicio.InicioViewModel
import com.example.autotallerapp.ui.theme.NaranjaPendiente
import com.example.autotallerapp.ui.theme.NaranjaPendienteClaro

@Composable
fun PendienteScreen(
    irALogin: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(color = NaranjaPendienteClaro, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    tint = NaranjaPendiente,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Tu cuenta esta pendiente",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "El administrador del taller debe asignarte un rol antes de que puedas usar la app. " +
                    "Vuelve a iniciar sesion cuando te avise.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            TextButton(onClick = {
                viewModel.cerrarSesion()
                irALogin()
            }) { Text("Cerrar sesion") }
        }
    }
}

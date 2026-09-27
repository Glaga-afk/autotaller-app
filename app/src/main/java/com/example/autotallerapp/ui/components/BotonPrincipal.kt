package com.example.autotallerapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BotonPrincipal(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(texto, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icono?.invoke()
            Text(texto, style = MaterialTheme.typography.titleSmall)
        }
    }
}

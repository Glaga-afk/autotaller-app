package com.example.autotallerapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MinimalistButtonsExample(
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Botón Principal (Sólido con el nuevo Azul Minimalista)
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(text = "Acción Principal", style = MaterialTheme.typography.bodyLarge)
        }

        // 2. Botón Suave con Transparencia (Efecto translúcido al 10%)
        Button(
            onClick = onSecondaryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Acción Secundaria (Translúcida)", style = MaterialTheme.typography.bodyLarge)
        }

        // 3. Botón de Borde Sutil (Outlined minimalista)
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Cancelar / Volver", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
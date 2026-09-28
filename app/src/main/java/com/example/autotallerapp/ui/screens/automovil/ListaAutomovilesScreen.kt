package com.example.autotallerapp.ui.screens.automovil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.Automovil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAutomovilesScreen(
    volver: () -> Unit,
    irANuevoAutomovil: () -> Unit,
    viewModel: ListaAutomovilesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Recarga al abrir la pantalla y cada vez que se vuelve desde el formulario de registro
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automoviles") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = irANuevoAutomovil,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar automovil") }
            )
        }
    ) { relleno ->
        when {
            estado.cargando && estado.automoviles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(relleno),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            estado.mensajeError != null && estado.automoviles.isEmpty() -> {
                MensajeCentrado(
                    titulo = "No se pudo cargar",
                    detalle = estado.mensajeError.orEmpty(),
                    textoBoton = "Reintentar",
                    alPulsarBoton = viewModel::cargar,
                    modifier = Modifier.padding(relleno)
                )
            }

            estado.automoviles.isEmpty() -> {
                MensajeCentrado(
                    titulo = "Sin automoviles",
                    detalle = "Este cliente aun no tiene automoviles registrados.",
                    modifier = Modifier.padding(relleno)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(relleno),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.automoviles, key = { it.id }) { automovil ->
                        TarjetaAutomovil(automovil)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAutomovil(automovil: Automovil) {
    val detalle = listOfNotNull(
        automovil.anio?.toString(),
        automovil.color.ifBlank { null }
    ).joinToString(" · ")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(automovil.placa, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${automovil.marca} ${automovil.modelo}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (detalle.isNotEmpty()) {
                    Text(
                        detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MensajeCentrado(
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    textoBoton: String? = null,
    alPulsarBoton: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoBoton != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = alPulsarBoton) { Text(textoBoton) }
        }
    }
}
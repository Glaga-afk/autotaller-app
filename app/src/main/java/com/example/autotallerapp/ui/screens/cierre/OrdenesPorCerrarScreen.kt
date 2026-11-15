package com.example.autotallerapp.ui.screens.cierre

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.ui.components.BarraSuperior
import com.example.autotallerapp.ui.theme.PlayfairDisplay

/** HU08: el supervisor elige la OST en reparacion que va a cerrar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesPorCerrarScreen(
    volver: () -> Unit,
    irAInforme: (String) -> Unit,
    viewModel: OrdenesPorCerrarViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Se recarga cada vez que se vuelve a esta pantalla (por ejemplo, despues de cerrar una OST)
    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        topBar = {
            // Misma barra superior que el resto de la app
            BarraSuperior(
                titulo = "Cerrar OST",
                alVolver = volver,
                fuenteTitulo = PlayfairDisplay,
                iconoPantalla = Icons.Outlined.TaskAlt,
                acciones = {
                    IconButton(onClick = viewModel::cargar) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                estado.error != null -> MensajeCentral(
                    texto = estado.error.orEmpty(),
                    textoBoton = "Reintentar",
                    alPulsar = viewModel::cargar
                )

                estado.ordenes.isEmpty() -> MensajeCentral(
                    texto = "No hay ordenes en reparacion.\nCuando se asignen tecnicos a una OST aparecera aqui."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Ordenes en reparacion (${estado.ordenes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(estado.ordenes, key = { it.idLocal }) { orden ->
                        TarjetaOrdenPorCerrar(orden = orden, alPulsar = { irAInforme(orden.idLocal) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaOrdenPorCerrar(orden: OrdenServicio, alPulsar: () -> Unit) {
    Card(onClick = alPulsar, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    orden.numeroFormateado ?: "OST sin numero",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = alPulsar,
                    label = { Text(orden.estado.etiqueta) },
                    leadingIcon = { Icon(Icons.Outlined.Build, contentDescription = null, Modifier.size(18.dp)) }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DirectionsCar, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("${orden.placa} - ${orden.marca} ${orden.modelo}", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Cliente: ${orden.clienteNombre}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Falla: ${orden.falla}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MensajeCentral(texto: String, textoBoton: String? = null, alPulsar: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.TaskAlt,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(12.dp))
        Text(texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (textoBoton != null) {
            Spacer(Modifier.size(12.dp))
            Button(onClick = alPulsar) { Text(textoBoton) }
        }
    }
}

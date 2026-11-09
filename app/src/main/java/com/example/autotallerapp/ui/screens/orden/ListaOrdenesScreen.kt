package com.example.autotallerapp.ui.screens.orden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autotallerapp.domain.model.EstadoOrden

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaOrdenesScreen(
    volver: () -> Unit,
    irADetalleOrden: (String) -> Unit,
    viewModel: ListaOrdenesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Órdenes de Servicio Técnico") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
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
            Column(modifier = Modifier.fillMaxSize()) {
                if (estado.cargando && estado.ordenes.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        estado.cargando && estado.ordenes.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        estado.ordenes.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ListAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No hay órdenes registradas aún",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (estado.error != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        estado.error!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Text(
                                        "Historial y Órdenes Activas (${estado.ordenes.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                items(estado.ordenes, key = { it.idLocal }) { orden ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { irADetalleOrden(orden.idLocal) },
                                        shape = MaterialTheme.shapes.medium,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = orden.numero?.let { "OST #$it" } ?: "OST (Sin número)",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = orden.estado.name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Cliente: ${orden.clienteNombre}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Vehículo: ${orden.placa} (${orden.marca} ${orden.modelo})", style = MaterialTheme.typography.bodySmall)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // El botón solo se muestra si la orden NO está atendida
                                            if (orden.estado != EstadoOrden.ATENDIDA) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.actualizarEstadoOrden(
                                                                idOrdenLocal = orden.idLocal,
                                                                nuevoEstado = EstadoOrden.ATENDIDA
                                                            )
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Marcar Atendida")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
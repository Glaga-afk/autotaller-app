package com.example.autotallerapp.ui.screens.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.OrdenServicio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionOrdenScreen(
    volver: () -> Unit,
    alSeleccionarOrden: (String) -> Unit,
    viewModel: SeleccionOrdenViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostico con IA") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        when {
            estado.cargando && estado.ordenes.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.mensajeError != null && estado.ordenes.isEmpty() -> MensajeCentrado(
                titulo = "No se pudo cargar",
                detalle = estado.mensajeError.orEmpty(),
                textoBoton = "Reintentar",
                alPulsarBoton = viewModel::cargar,
                modifier = Modifier.padding(relleno)
            )

            estado.ordenes.isEmpty() -> MensajeCentrado(
                titulo = "Sin ordenes",
                detalle = "Aun no hay ordenes registradas para diagnosticar.",
                modifier = Modifier.padding(relleno)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Elige la orden que quieres diagnosticar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(estado.ordenes, key = { it.idLocal }) { orden ->
                    TarjetaOrden(orden = orden, alPulsar = { alSeleccionarOrden(orden.idLocal) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaOrden(orden: OrdenServicio, alPulsar: () -> Unit) {
    val fecha = remember(orden.fechaRegistro) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-PE"))
            .format(Date(orden.fechaRegistro))
    }

    Card(
        onClick = alPulsar,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    orden.numeroFormateado ?: "OST sin numero",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        orden.estado.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                "${orden.placa} · ${orden.marca} ${orden.modelo}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                orden.falla,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
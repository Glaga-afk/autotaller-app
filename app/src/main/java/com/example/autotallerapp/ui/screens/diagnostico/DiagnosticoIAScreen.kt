package com.example.autotallerapp.ui.screens.diagnostico

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.FotoEvidencia
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.Urgencia
import com.example.autotallerapp.ui.components.BotonPrincipal
import com.example.autotallerapp.ui.components.BotonSecundario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticoIAScreen(
    volver: () -> Unit,
    viewModel: DiagnosticoIAViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    if (estado.guardado) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
            title = { Text("Diagnostico guardado") },
            text = {
                Text("Quedo vinculado a la orden ${estado.orden?.numeroFormateado.orEmpty()}".trim() + ".")
            },
            confirmButton = { TextButton(onClick = volver) { Text("Aceptar") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
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
            estado.cargandoOrden -> Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.errorOrden != null -> Column(
                modifier = Modifier.fillMaxSize().padding(relleno).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(estado.errorOrden.orEmpty(), textAlign = TextAlign.Center)
                TextButton(onClick = viewModel::cargarOrden) { Text("Reintentar") }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                estado.orden?.let { ResumenOrden(it) }
                FotosOrden(estado.fotos)
                SeccionDiagnostico(
                    estado = estado,
                    alReintentar = viewModel::diagnosticar,
                    alElegirSistema = viewModel::alElegirSistema,
                    alCambiarCausa = viewModel::alCambiarCausa,
                    alElegirUrgencia = viewModel::alElegirUrgencia,
                    alGuardar = viewModel::guardar
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Datos de la orden
// ---------------------------------------------------------------------------

@Composable
private fun ResumenOrden(orden: OrdenServicio) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                orden.numeroFormateado ?: "OST sin numero",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${orden.placa} · ${orden.marca} ${orden.modelo}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Cliente: ${orden.clienteNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text("Falla reportada", style = MaterialTheme.typography.labelLarge)
            Text(orden.falla, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FotosOrden(fotos: List<FotoEvidencia>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fotos de la falla (${fotos.size})", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fotos.forEach { MiniaturaFoto(it.base64) }
        }
    }
}

@Composable
private fun MiniaturaFoto(base64: String) {
    val imagen = remember(base64) {
        runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            // inSampleSize = 2: la miniatura no necesita los 1280 px completos
            val opciones = BitmapFactory.Options().apply { inSampleSize = 2 }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)?.asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imagen != null) {
            Image(
                bitmap = imagen,
                contentDescription = "Foto de la falla",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("Sin vista previa", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ---------------------------------------------------------------------------
// Diagnostico de la IA
// ---------------------------------------------------------------------------

@Composable
private fun SeccionDiagnostico(
    estado: DiagnosticoIAUiState,
    alReintentar: () -> Unit,
    alElegirSistema: (SistemaAutomovil) -> Unit,
    alCambiarCausa: (String) -> Unit,
    alElegirUrgencia: (Urgencia) -> Unit,
    alGuardar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Diagnostico sugerido por IA", style = MaterialTheme.typography.titleMedium)
        }

        when {
            estado.analizando -> Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Analizando la foto y la descripcion con Gemini...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            estado.errorIa != null -> Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        estado.errorIa.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = alReintentar) { Text("Reintentar") }
                }
            }

            estado.sugerencia != null -> FormularioDiagnostico(
                estado = estado,
                alReanalizar = alReintentar,
                alElegirSistema = alElegirSistema,
                alCambiarCausa = alCambiarCausa,
                alElegirUrgencia = alElegirUrgencia,
                alGuardar = alGuardar
            )
        }
    }
}

@Composable
private fun FormularioDiagnostico(
    estado: DiagnosticoIAUiState,
    alReanalizar: () -> Unit,
    alElegirSistema: (SistemaAutomovil) -> Unit,
    alCambiarCausa: (String) -> Unit,
    alElegirUrgencia: (Urgencia) -> Unit,
    alGuardar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sistema afectado", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SistemaAutomovil.entries.forEach { sistema ->
                FilterChip(
                    selected = estado.sistema == sistema,
                    onClick = { alElegirSistema(sistema) },
                    label = { Text(sistema.etiqueta) }
                )
            }
        }

        OutlinedTextField(
            value = estado.causa,
            onValueChange = alCambiarCausa,
            label = { Text("Causa probable") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Urgencia", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Urgencia.entries.forEach { urgencia ->
                val color = colorDe(urgencia)
                FilterChip(
                    selected = estado.urgencia == urgencia,
                    onClick = { alElegirUrgencia(urgencia) },
                    label = { Text(urgencia.etiqueta, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = color.copy(alpha = 0.12f),
                        labelColor = color,
                        selectedContainerColor = color,
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(1.dp, color)
                )
            }
        }

        if (estado.corregido) {
            Text(
                "Editaste la sugerencia de la IA. Se guardaran las dos versiones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        CasosSimilares(estado.casosSimilares)

        Text(
            "Sugerencia generada por IA. Verificala antes de reparar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BotonPrincipal(
            texto = "Guardar diagnostico",
            alPulsar = alGuardar,
            cargando = estado.guardando,
            habilitado = estado.causa.isNotBlank()
        )
        BotonSecundario(
            texto = "Volver a analizar",
            alPulsar = alReanalizar,
            habilitado = !estado.guardando
        )
    }
}

@Composable
private fun CasosSimilares(casos: List<ProblemaTecnico>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Casos parecidos en la bitacora", style = MaterialTheme.typography.titleSmall)

        if (casos.isEmpty()) {
            Text(
                "No hay casos parecidos registrados. Podria ser un problema nuevo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            casos.forEach { caso ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(caso.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${caso.marca} ${caso.modelo}".trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Solucion: ${caso.solucion}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun colorDe(urgencia: Urgencia): Color = when (urgencia) {
    Urgencia.BAJA -> Color(0xFF2E7D32)
    Urgencia.MEDIA -> Color(0xFFB45309)
    Urgencia.ALTA -> Color(0xFFEA580C)
    Urgencia.CRITICA -> Color(0xFFB91C1C)
}
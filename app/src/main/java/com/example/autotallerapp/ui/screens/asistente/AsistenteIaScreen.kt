package com.example.autotallerapp.ui.screens.asistente

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.RolChat
import com.example.autotallerapp.ui.components.BarraSuperior
import com.example.autotallerapp.ui.theme.PlayfairDisplay

private val SUGERENCIAS = listOf(
    "El pedal de freno se siente esponjoso, ¿qué reviso primero?",
    "El motor se calienta en tráfico, ¿cuáles pueden ser las causas?",
    "La batería se descarga sola, ¿cómo la diagnostico?"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenteIaScreen(
    volver: () -> Unit,
    viewModel: AsistenteIaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val listaState = rememberLazyListState()
    var confirmarBorrado by remember { mutableStateOf(false) }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    // Baja al ultimo elemento cuando llega un mensaje, aparece "Pensando..." o un error
    val extras = (if (estado.respondiendo) 1 else 0) + (if (estado.errorIa != null) 1 else 0)
    LaunchedEffect(estado.mensajes.size, extras) {
        val total = estado.mensajes.size + extras
        if (total > 0) listaState.animateScrollToItem(total - 1)
    }

    val permisoMicrofono = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            viewModel.iniciarEscucha()
        } else {
            viewModel.mostrarMensaje("Se necesita permiso de micrófono para dictar")
        }
    }

    fun alPulsarMicrofono() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        when {
            estado.escuchando -> viewModel.detenerEscucha()
            tienePermiso -> viewModel.iniciarEscucha()
            else -> permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (estado.mostrarSelectorOrden) {
        DialogoSelectorOrden(
            ordenes = estado.ordenesDisponibles,
            cargando = estado.cargandoOrdenes,
            error = estado.errorOrdenes,
            ordenActualId = estado.orden?.idLocal,
            alElegir = viewModel::elegirOrden,
            alReintentar = viewModel::cargarOrdenes,
            alCerrar = viewModel::cerrarSelectorOrden
        )
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Borrar historial") },
            text = { Text("Se borrará esta conversación del celular.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    viewModel.borrarConversacion()
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BarraSuperior(
                titulo = "Asistente técnico",
                alVolver = volver,
                fuenteTitulo = PlayfairDisplay,
                iconoPantalla = Icons.Outlined.Chat,
                acciones = {
                    IconButton(
                        onClick = { confirmarBorrado = true },
                        enabled = estado.mensajes.isNotEmpty()
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Borrar historial")
                    }
                }
            )
        },
        bottomBar = {
            BarraEntrada(
                entrada = estado.entrada,
                escuchando = estado.escuchando,
                respondiendo = estado.respondiendo,
                vozDisponible = estado.vozDisponible,
                alCambiar = viewModel::alCambiarEntrada,
                alEnviar = viewModel::enviar,
                alMicrofono = ::alPulsarMicrofono
            )
        }
    ) { relleno ->
        Column(modifier = Modifier.fillMaxSize().padding(relleno)) {
            BarraContexto(orden = estado.orden, alCambiar = viewModel::abrirSelectorOrden)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (estado.mensajes.isEmpty() && !estado.respondiendo) {
                    PantallaInicial(alElegir = viewModel::enviarSugerencia)
                } else {
                    LazyColumn(
                        state = listaState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(estado.mensajes, key = { it.id }) { mensaje ->
                            BurbujaMensaje(mensaje)
                        }
                        if (estado.respondiendo) {
                            item { BurbujaPensando() }
                        }
                        estado.errorIa?.let { error ->
                            item { TarjetaError(mensaje = error, alReintentar = viewModel::reintentar) }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Contexto y pantalla inicial
// ---------------------------------------------------------------------------

@Composable
private fun BarraContexto(orden: OrdenServicio?, alCambiar: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Contexto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = orden?.let {
                        "${it.numeroFormateado ?: "OST sin número"} · ${it.placa} · ${it.marca} ${it.modelo}"
                    } ?: "Consulta general (sin orden)",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = alCambiar) { Text("Cambiar") }
        }
    }
}

@Composable
private fun PantallaInicial(alElegir: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Pregúntale al asistente", style = MaterialTheme.typography.titleMedium)
        Text(
            "Escribe o toca el micrófono. Usa la orden elegida y la bitácora del taller.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        SUGERENCIAS.forEach { sugerencia ->
            SuggestionChip(
                onClick = { alElegir(sugerencia) },
                label = { Text(sugerencia) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Mensajes
// ---------------------------------------------------------------------------

@Composable
private fun BurbujaMensaje(mensaje: MensajeChat) {
    val esUsuario = mensaje.rol == RolChat.USUARIO

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esUsuario) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (esUsuario) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esUsuario) 16.dp else 4.dp,
                bottomEnd = if (esUsuario) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = mensaje.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = if (esUsuario) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(12.dp)
            )
        }

        if (!esUsuario && mensaje.fuentes.isNotEmpty()) {
            Text(
                text = "Bitácora consultada: ${mensaje.fuentes.joinToString(" · ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp).widthIn(max = 300.dp)
            )
        }
    }
}

@Composable
private fun BurbujaPensando() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Pensando...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TarjetaError(mensaje: String, alReintentar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = alReintentar) { Text("Reintentar") }
        }
    }
}

// ---------------------------------------------------------------------------
// Barra de entrada (texto y voz)
// ---------------------------------------------------------------------------

@Composable
private fun BarraEntrada(
    entrada: String,
    escuchando: Boolean,
    respondiendo: Boolean,
    vozDisponible: Boolean,
    alCambiar: (String) -> Unit,
    alEnviar: () -> Unit,
    alMicrofono: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.navigationBarsPadding().imePadding()
    ) {
        Column {
            if (escuchando) {
                Text(
                    "Escuchando... habla ahora y toca el cuadro para terminar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = entrada,
                    onValueChange = alCambiar,
                    placeholder = { Text("Escribe tu consulta") },
                    maxLines = 4,
                    enabled = !respondiendo,
                    modifier = Modifier.weight(1f)
                )
                if (vozDisponible) {
                    IconButton(onClick = alMicrofono, enabled = !respondiendo) {
                        Icon(
                            imageVector = if (escuchando) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (escuchando) "Terminar de hablar" else "Dictar consulta",
                            tint = if (escuchando) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                IconButton(
                    onClick = alEnviar,
                    enabled = entrada.isNotBlank() && !respondiendo
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Selector de orden
// ---------------------------------------------------------------------------

@Composable
private fun DialogoSelectorOrden(
    ordenes: List<OrdenServicio>,
    cargando: Boolean,
    error: String?,
    ordenActualId: String?,
    alElegir: (OrdenServicio?) -> Unit,
    alReintentar: () -> Unit,
    alCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Contexto de la consulta") },
        text = {
            when {
                cargando -> Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Column {
                    Text(error, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = alReintentar) { Text("Reintentar") }
                }

                else -> LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    item {
                        FilaOpcion(
                            titulo = "Consulta general (sin orden)",
                            detalle = "Sin datos de un vehículo",
                            seleccionada = ordenActualId == null,
                            alPulsar = { alElegir(null) }
                        )
                    }
                    items(ordenes, key = { it.idLocal }) { orden ->
                        FilaOpcion(
                            titulo = "${orden.numeroFormateado ?: "OST sin número"} · ${orden.placa}",
                            detalle = "${orden.marca} ${orden.modelo} · ${orden.estado.etiqueta}",
                            seleccionada = orden.idLocal == ordenActualId,
                            alPulsar = { alElegir(orden) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cerrar") } }
    )
}

@Composable
private fun FilaOpcion(
    titulo: String,
    detalle: String,
    seleccionada: Boolean,
    alPulsar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
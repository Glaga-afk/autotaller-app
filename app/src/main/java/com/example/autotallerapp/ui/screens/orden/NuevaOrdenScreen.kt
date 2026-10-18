package com.example.autotallerapp.ui.screens.orden

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.autotallerapp.data.device.GrabadorAudio
import com.example.autotallerapp.data.device.ProveedorUbicacion
import com.example.autotallerapp.domain.model.EstadoEnvio
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.usecase.RegistrarOrdenUseCase
import com.example.autotallerapp.ui.components.BotonPrincipal
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaOrdenScreen(
    fotoCapturada: String?,
    alConsumirFoto: () -> Unit,
    volver: () -> Unit,
    irACamara: () -> Unit,
    alFinalizar: () -> Unit,
    viewModel: NuevaOrdenViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    // La camara devuelve la ruta de la foto por el back stack; aqui se agrega a la orden
    LaunchedEffect(fotoCapturada) {
        fotoCapturada?.let {
            viewModel.agregarFoto(it)
            alConsumirFoto()
        }
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    val permisoMicrofono = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            viewModel.iniciarGrabacion()
        } else {
            viewModel.mostrarMensaje("Se necesita permiso de microfono para grabar la nota de voz")
        }
    }

    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) {
            viewModel.alCambiarUbicacion(true)
        } else {
            viewModel.mostrarMensaje("Se necesita permiso de ubicacion")
        }
    }

    estado.ordenRegistrada?.let { orden ->
        DialogoOrdenRegistrada(orden = orden, alFinalizar = alFinalizar)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Nueva orden de servicio") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        when {
            estado.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.errorCarga != null -> Column(
                modifier = Modifier.fillMaxSize().padding(relleno).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(estado.errorCarga.orEmpty(), textAlign = TextAlign.Center)
                TextButton(onClick = viewModel::cargarDatos) { Text("Reintentar") }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResumenOrden(estado)

                SeccionFalla(falla = estado.falla, alCambiar = viewModel::alCambiarFalla)

                SeccionFotos(
                    fotos = estado.fotos,
                    puedeAgregar = estado.puedeAgregarFoto,
                    alAgregar = irACamara,
                    alQuitar = viewModel::quitarFoto
                )

                SeccionNotaVoz(
                    estado = estado,
                    alGrabar = {
                        if (tienePermiso(contexto, Manifest.permission.RECORD_AUDIO)) {
                            viewModel.iniciarGrabacion()
                        } else {
                            permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    alDetener = viewModel::detenerGrabacion,
                    alReproducir = viewModel::alternarReproduccion,
                    alEliminar = viewModel::eliminarAudio
                )

                SeccionUbicacion(
                    estado = estado,
                    alCambiar = { activar ->
                        when {
                            !activar -> viewModel.alCambiarUbicacion(false)
                            ProveedorUbicacion.PERMISOS.any { tienePermiso(contexto, it) } ->
                                viewModel.alCambiarUbicacion(true)
                            else -> permisoUbicacion.launch(ProveedorUbicacion.PERMISOS)
                        }
                    }
                )

                BotonPrincipal(
                    texto = "Registrar OST",
                    alPulsar = viewModel::registrar,
                    cargando = estado.guardando,
                    habilitado = !estado.grabando
                )

                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

private fun tienePermiso(contexto: Context, permiso: String): Boolean =
    ContextCompat.checkSelfPermission(contexto, permiso) == PackageManager.PERMISSION_GRANTED

private fun formatearSegundos(segundos: Int): String =
    String.format(Locale.ROOT, "%d:%02d", segundos / 60, segundos % 60)

// ---------------------------------------------------------------------------
// Secciones del formulario
// ---------------------------------------------------------------------------

@Composable
private fun ResumenOrden(estado: NuevaOrdenUiState) {
    val fechaHora = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-PE")).format(Date())
    }
    val cliente = estado.cliente
    val automovil = estado.automovil

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilaDato(
                icono = Icons.Outlined.Person,
                titulo = cliente?.nombreCompleto.orEmpty(),
                detalle = "DNI ${cliente?.dni.orEmpty()}"
            )
            FilaDato(
                icono = Icons.Outlined.DirectionsCar,
                titulo = automovil?.placa.orEmpty(),
                detalle = "${automovil?.marca.orEmpty()} ${automovil?.modelo.orEmpty()}"
            )
            FilaDato(
                icono = Icons.Outlined.Schedule,
                titulo = fechaHora,
                detalle = "Se registrara con estado Pendiente"
            )
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, titulo: String, detalle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun SeccionFalla(falla: String, alCambiar: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion("Falla reportada")
        OutlinedTextField(
            value = falla,
            onValueChange = alCambiar,
            placeholder = { Text("Ruido metalico al frenar, mas fuerte en la rueda delantera izquierda") },
            supportingText = {
                Text("Minimo ${RegistrarOrdenUseCase.MIN_CARACTERES_FALLA} caracteres")
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
        )
    }
}

@Composable
private fun SeccionFotos(
    fotos: List<String>,
    puedeAgregar: Boolean,
    alAgregar: () -> Unit,
    alQuitar: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion("Fotos del automovil (${fotos.size}/${RegistrarOrdenUseCase.MAX_FOTOS})")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fotos.forEach { ruta ->
                Box(modifier = Modifier.size(104.dp)) {
                    AsyncImage(
                        model = File(ruta),
                        contentDescription = "Foto del automovil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                    Surface(
                        onClick = { alQuitar(ruta) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Quitar foto",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (puedeAgregar) {
                OutlinedCard(onClick = alAgregar, modifier = Modifier.size(104.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Agregar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionNotaVoz(
    estado: NuevaOrdenUiState,
    alGrabar: () -> Unit,
    alDetener: () -> Unit,
    alReproducir: () -> Unit,
    alEliminar: () -> Unit
) {
    val limite = formatearSegundos(GrabadorAudio.DURACION_MAXIMA_MS / 1_000)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion("Nota de voz (opcional)")
        when {
            estado.grabando -> Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.FiberManualRecord,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Grabando ${formatearSegundos(estado.segundosGrabados)} / $limite",
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(onClick = alDetener) { Text("Detener") }
            }

            estado.audio != null -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = alReproducir) {
                    Icon(
                        if (estado.reproduciendo) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (estado.reproduciendo) "Detener" else "Escuchar"
                    )
                }
                Text(
                    "Nota de voz (${formatearSegundos(estado.segundosGrabados)})",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = alEliminar) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar nota de voz")
                }
            }

            else -> OutlinedButton(onClick = alGrabar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Grabar nota de voz (max. $limite)")
            }
        }
    }
}

@Composable
private fun SeccionUbicacion(estado: NuevaOrdenUiState, alCambiar: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                TituloSeccion("Ubicacion del vehiculo")
                Text(
                    "Activala si es un auxilio a domicilio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = estado.incluirUbicacion,
                onCheckedChange = alCambiar,
                enabled = !estado.obteniendoUbicacion
            )
        }

        if (estado.obteniendoUbicacion) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        estado.ubicacion?.takeIf { estado.incluirUbicacion }?.let { ubicacion ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    String.format(
                        Locale.ROOT, "Lat %.5f, Lon %.5f", ubicacion.latitud, ubicacion.longitud
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DialogoOrdenRegistrada(orden: OrdenServicio, alFinalizar: () -> Unit) {
    val detalleEnvio = when (orden.estadoEnvio) {
        EstadoEnvio.ENVIADA ->
            "Enviada al taller. El tecnico supervisor ya puede verla."
        EstadoEnvio.ERROR ->
            "Guardada en el celular. El envio fallo y se reintentara automaticamente."
        EstadoEnvio.PENDIENTE_ENVIO ->
            "Guardada en el celular. Se enviara y numerara en cuanto haya conexion."
    }

    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
        title = { Text("OST registrada") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    orden.numeroFormateado ?: "Numero pendiente",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Estado: ${orden.estado.etiqueta}")
                Text(
                    detalleEnvio,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = alFinalizar) { Text("Finalizar") }
        }
    )
}

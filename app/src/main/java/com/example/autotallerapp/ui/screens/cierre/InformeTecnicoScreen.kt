package com.example.autotallerapp.ui.screens.cierre

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.SistemaAutomovil
import com.example.autotallerapp.domain.model.TrabajoTecnico
import com.example.autotallerapp.ui.components.BarraSuperior
import com.example.autotallerapp.ui.theme.PlayfairDisplay

/** HU08: informe tecnico, cierre de la OST y problema nuevo para la bitacora. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeTecnicoScreen(
    volver: () -> Unit,
    alFinalizar: () -> Unit,
    viewModel: InformeTecnicoViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            // Misma barra superior que el resto de la app; al cerrar la OST ya no se puede volver
            BarraSuperior(
                titulo = estado.orden?.numeroFormateado ?: "Informe tecnico",
                alVolver = if (estado.cerrada) null else volver,
                fuenteTitulo = PlayfairDisplay,
                iconoPantalla = Icons.Outlined.Engineering
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                estado.errorCarga != null -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(estado.errorCarga.orEmpty(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::cargar) { Text("Reintentar") }
                }

                estado.cerrada -> PanelOrdenCerrada(estado = estado, alFinalizar = alFinalizar)

                else -> FormularioInforme(estado = estado, viewModel = viewModel)
            }
        }
    }

    if (estado.confirmandoCierre) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarConfirmacion,
            title = { Text("Cerrar la OST") },
            text = {
                Text(
                    "La orden pasara a estado Atendida y el informe ya no se podra modificar." +
                        if (estado.registrarProblema) " Tambien se registrara el problema en la bitacora." else ""
                )
            },
            confirmButton = { Button(onClick = viewModel::cerrar) { Text("Cerrar OST") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FormularioInforme(estado: InformeTecnicoUiState, viewModel: InformeTecnicoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        estado.orden?.let { DatosOrden(it) }

        // 1. Solucion aplicada
        Titulo("Solucion aplicada")
        OutlinedTextField(
            value = estado.solucion,
            onValueChange = viewModel::alCambiarSolucion,
            label = { Text("Detalle de la reparacion realizada") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Tecnicos y trabajo realizado
        Titulo("Tecnicos y trabajo realizado")
        if (estado.trabajos.isEmpty()) {
            Aviso("Esta OST no tiene tecnicos asignados. Primero asignalos (HU06).")
        }
        estado.trabajos.forEach { trabajo ->
            TarjetaTrabajo(
                trabajo = trabajo,
                alCambiar = { viewModel.alCambiarTrabajo(trabajo.tecnicoId, it) }
            )
        }

        // 3. Problema nuevo para la bitacora
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Problema nuevo", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Registrarlo en la bitacora si todavia no existe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = estado.registrarProblema, onCheckedChange = viewModel::alCambiarRegistrarProblema)
        }
        if (estado.registrarProblema) {
            SeccionProblemaNuevo(estado = estado, viewModel = viewModel)
        }

        Button(
            onClick = viewModel::pedirConfirmacion,
            enabled = !estado.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrar informe y cerrar OST")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DatosOrden(orden: OrdenServicio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${orden.placa} - ${orden.marca} ${orden.modelo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Cliente: ${orden.clienteNombre} (DNI ${orden.clienteDni})", style = MaterialTheme.typography.bodyMedium)
            Text("Estado: ${orden.estado.etiqueta}", style = MaterialTheme.typography.bodyMedium)
            Text("Falla reportada: ${orden.falla}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TarjetaTrabajo(trabajo: TrabajoTecnico, alCambiar: (String) -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Engineering, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(trabajo.nombreTecnico, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tarea asignada: ${trabajo.tareaAsignada}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedTextField(
                value = trabajo.trabajoRealizado,
                onValueChange = alCambiar,
                label = { Text("Trabajo que realizo") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionProblemaNuevo(estado: InformeTecnicoUiState, viewModel: InformeTecnicoViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sistema del automovil", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SistemaAutomovil.entries.forEach { sistema ->
                FilterChip(
                    selected = estado.sistema == sistema,
                    onClick = { viewModel.alSeleccionarSistema(sistema) },
                    label = { Text(sistema.etiqueta) }
                )
            }
        }
        OutlinedTextField(
            value = estado.tituloProblema,
            onValueChange = viewModel::alCambiarTitulo,
            label = { Text("Titulo del problema") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.causaProblema,
            onValueChange = viewModel::alCambiarCausa,
            label = { Text("Causa del problema") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "La solucion que se guardara en la bitacora es la solucion aplicada de arriba.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (estado.similares.isNotEmpty()) {
            Aviso("Ya hay problemas parecidos en la bitacora. Si es el mismo, desactiva \"Problema nuevo\".")
            estado.similares.take(3).forEach { problema ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(problema.titulo, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${problema.sistema.etiqueta} - ${problema.marca} ${problema.modelo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/** Se muestra cuando la OST ya quedo en estado Atendida. */
@Composable
private fun PanelOrdenCerrada(estado: InformeTecnicoUiState, alFinalizar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text("OST cerrada", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${estado.orden?.numeroFormateado.orEmpty()} quedo en estado Atendida con su informe tecnico.",
            textAlign = TextAlign.Center
        )
        if (estado.problemaRegistrado) {
            Text(
                "El problema nuevo se registro en la bitacora.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = alFinalizar, modifier = Modifier.fillMaxWidth()) { Text("Finalizar") }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Aviso(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

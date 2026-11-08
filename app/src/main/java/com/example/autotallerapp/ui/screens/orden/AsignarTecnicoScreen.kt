package com.example.autotallerapp.ui.screens.orden

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autotallerapp.domain.model.Tecnico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarTecnicoScreen(
    idOrdenLocal: String,
    onVolver: () -> Unit,
    onAsignacionExitosa: () -> Unit,
    viewModel: AsignarTecnicoViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState(initial = AsignarTecnicoUiState.Cargando)
    // Especificamos explícitamente el tipo de mapa vacío para ayudar al compilador
    val asignaciones by viewModel.asignacionesTemporales.collectAsState(initial = emptyMap<String, String>())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignar Técnicos a la OST") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        // Usamos la versión AutoMirrored recomendada
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is AsignarTecnicoUiState.Exito) {
                val tecnicos = (uiState as AsignarTecnicoUiState.Exito).tecnicos
                FloatingActionButton(
                    onClick = {
                        viewModel.confirmarAsignacion(
                            idOrdenLocal = idOrdenLocal,
                            listaTecnicosDisponibles = tecnicos,
                            onAsignacionExitosa = {
                                Toast.makeText(context, "Técnicos asignados con éxito", Toast.LENGTH_SHORT).show()
                                onAsignacionExitosa()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar Asignación")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val estado = uiState) {
                is AsignarTecnicoUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AsignarTecnicoUiState.Error -> {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is AsignarTecnicoUiState.Exito -> {
                    if (estado.tecnicos.isEmpty()) {
                        Text(
                            text = "No hay técnicos disponibles registrados en el sistema.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Seleccione los técnicos y detalle el trabajo asignado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(estado.tecnicos) { tecnico ->
                                ItemTecnicoAsignacion(
                                    tecnico = tecnico,
                                    tareaActual = asignaciones[tecnico.id] ?: "",
                                    onTareaChanged = { nuevaTarea ->
                                        viewModel.actualizarTareaTecnico(tecnico.id, nuevaTarea)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemTecnicoAsignacion(
    tecnico: Tecnico,
    tareaActual: String,
    onTareaChanged: (String) -> Unit
) {
    val seleccionado = tareaActual.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = tecnico.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Especialidad: ${tecnico.especialidad}", style = MaterialTheme.typography.bodySmall)
                }
                Badge(
                    containerColor = if (tecnico.disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (tecnico.disponible) "Disponible" else "Ocupado",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tareaActual,
                onValueChange = onTareaChanged,
                label = { Text("Tarea o trabajo asignado") },
                placeholder = { Text("Ej: Revisión del sistema de frenos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
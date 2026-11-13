package com.example.autotallerapp.ui.screens.orden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autotallerapp.ui.cliente.ClienteViewModel
import com.example.autotallerapp.ui.cliente.ClienteUiState
import com.example.autotallerapp.ui.cliente.DialogoFormularioCliente
import com.example.autotallerapp.ui.components.BarraSuperior
import com.example.autotallerapp.ui.theme.PlayfairDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaOstClienteScreen(
    viewModel: ClienteViewModel = hiltViewModel(),
    onVolver: () -> Unit,
    onClienteSeleccionado: (String) -> Unit // Navega hacia la selección de vehículos del cliente
) {
    val uiState by viewModel.uiState.collectAsState()
    var dniQuery by remember { mutableStateOf("") }
    var mostrarDialogoRegistro by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Nueva OST",
                alVolver = onVolver,
                fuenteTitulo = PlayfairDisplay,
                iconoPantalla = Icons.Outlined.NoteAdd
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Ingrese el DNI del cliente para iniciar la Orden de Servicio Técnico:",
                style = MaterialTheme.typography.bodyLarge
            )

            // Barra de búsqueda exclusiva para DNI en Nueva OST
            OutlinedTextField(
                value = dniQuery,
                onValueChange = {
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        dniQuery = it
                        if (it.length == 8) {
                            viewModel.buscarPorDni(it)
                        }
                    }
                },
                label = { Text("Número de DNI (8 dígitos)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            // Estados de la búsqueda en esta pantalla única
            when (val state = uiState) {
                is ClienteUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ClienteUiState.ClienteEncontrado -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Cliente Encontrado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Nombre: ${state.cliente.nombreCompleto}", style = MaterialTheme.typography.bodyLarge)
                            Text("Teléfono: ${state.cliente.telefono}", style = MaterialTheme.typography.bodyMedium)

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onClienteSeleccionado(state.cliente.dni) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuar con este Cliente")
                            }
                        }
                    }
                }
                is ClienteUiState.ClienteNoEncontrado -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("No se encontró un cliente con el DNI: ${state.dniBuscado}", color = MaterialTheme.colorScheme.error)
                            Button(
                                onClick = { mostrarDialogoRegistro = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Registrar cliente ahora")
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // Formulario flotante si el cliente no existe y decide registrarlo en el momento
        if (mostrarDialogoRegistro) {
            DialogoFormularioCliente(
                titulo = "Registrar Cliente para la OST",
                dniInicial = dniQuery,
                onDismiss = { mostrarDialogoRegistro = false },
                onGuardar = { dni, nombre, direccion, sexo, telefono ->
                    viewModel.registrarCliente(dni, nombre, direccion, sexo, telefono)
                    mostrarDialogoRegistro = false
                }
            )
        }
    }
}
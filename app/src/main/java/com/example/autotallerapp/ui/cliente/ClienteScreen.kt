package com.example.autotallerapp.ui.cliente

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel = hiltViewModel(),
    onClienteConfirmado: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var dniQuery by remember { mutableStateOf("") }
    var nombreInput by remember { mutableStateOf("") }
    var direccionInput by remember { mutableStateOf("") }
    var sexoInput by remember { mutableStateOf("Masculino") }
    var telefonoInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ClienteUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ClienteUiState.Error).mensaje)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clientes", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Búsqueda Minimalista
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Búsqueda de Cliente", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                    OutlinedTextField(
                        value = dniQuery,
                        onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) dniQuery = it },
                        label = { Text("Número de DNI (8 dígitos)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    Button(
                        onClick = { viewModel.buscarPorDni(dniQuery) },
                        enabled = dniQuery.length == 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    ) {
                        Text("Verificar DNI", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            AnimatedContent(targetState = uiState, label = "StateAnim") { state ->
                when (state) {
                    is ClienteUiState.Cargando -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is ClienteUiState.ClienteEncontrado -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cliente ya registrado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("DNI: ${state.cliente.dni}", style = MaterialTheme.typography.bodyMedium)
                                Text("Nombre: ${state.cliente.nombreCompleto}", style = MaterialTheme.typography.bodyLarge)
                                Text("Teléfono: ${state.cliente.telefono}", style = MaterialTheme.typography.bodyMedium)
                                Text("Dirección: ${state.cliente.direccion}", style = MaterialTheme.typography.bodyMedium)

                                Spacer(Modifier.height(4.dp))

                                Button(
                                    onClick = { onClienteConfirmado(state.cliente.dni) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text("Seleccionar Cliente")
                                }
                            }
                        }
                    }
                    is ClienteUiState.ClienteNoEncontrado -> {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    "Cliente no registrado. Ingrese los datos:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                OutlinedTextField(
                                    value = state.dniBuscado,
                                    onValueChange = {},
                                    enabled = false,
                                    label = { Text("DNI") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = nombreInput,
                                    onValueChange = { nombreInput = it },
                                    label = { Text("Nombre Completo *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = direccionInput,
                                    onValueChange = { direccionInput = it },
                                    label = { Text("Dirección *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = telefonoInput,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) telefonoInput = it },
                                    label = { Text("Teléfono / Celular *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                Text("Sexo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = sexoInput == "Masculino",
                                        onClick = { sexoInput = "Masculino" },
                                        label = { Text("Masculino") }
                                    )
                                    FilterChip(
                                        selected = sexoInput == "Femenino",
                                        onClick = { sexoInput = "Femenino" },
                                        label = { Text("Femenino") }
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.registrarCliente(
                                            dni = state.dniBuscado,
                                            nombre = nombreInput,
                                            direccion = direccionInput,
                                            sexo = sexoInput,
                                            telefono = telefonoInput
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text("Guardar y Registrar Cliente")
                                }
                            }
                        }
                    }
                    is ClienteUiState.RegistroExitoso -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("¡Cliente registrado con éxito!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)

                                // Botón secundario con transparencia/estilo suave
                                Button(
                                    onClick = {
                                        viewModel.limpiarEstado()
                                        dniQuery = ""
                                        nombreInput = ""
                                        direccionInput = ""
                                        telefonoInput = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Realizar otra consulta")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
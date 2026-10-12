package com.example.autotallerapp.ui.cliente

import androidx.compose.animation.*
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
                title = { Text("Gestión de Clientes — HU02") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Búsqueda de Cliente", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = dniQuery,
                        onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) dniQuery = it },
                        label = { Text("Número de DNI (8 dígitos)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.buscarPorDni(dniQuery) },
                        enabled = dniQuery.length == 8,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Verificar DNI")
                    }
                }
            }

            AnimatedContent(targetState = uiState, label = "StateAnim") { state ->
                when (state) {
                    is ClienteUiState.Cargando -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ClienteUiState.ClienteEncontrado -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cliente ya registrado", style = MaterialTheme.typography.titleSmall)
                                }
                                Text("DNI: ${state.cliente.dni}")
                                Text("Nombre: ${state.cliente.nombreCompleto}")
                                Text("Teléfono: ${state.cliente.telefono}")
                                Text("Dirección: ${state.cliente.direccion}")
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onClienteConfirmado(state.cliente.dni) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Seleccionar Cliente")
                                }
                            }
                        }
                    }
                    is ClienteUiState.ClienteNoEncontrado -> {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = nombreInput,
                                    onValueChange = { nombreInput = it },
                                    label = { Text("Nombre Completo *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = direccionInput,
                                    onValueChange = { direccionInput = it },
                                    label = { Text("Dirección *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = telefonoInput,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) telefonoInput = it },
                                    label = { Text("Teléfono / Celular *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Sexo", style = MaterialTheme.typography.labelLarge)
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Guardar y Registrar Cliente")
                                }
                            }
                        }
                    }
                    is ClienteUiState.RegistroExitoso -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp))
                                Text("¡Cliente registrado con éxito!", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = {
                                    viewModel.limpiarEstado()
                                    dniQuery = ""
                                    nombreInput = ""
                                    direccionInput = ""
                                    telefonoInput = ""
                                }) {
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
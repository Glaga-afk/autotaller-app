package com.example.autotallerapp.ui.cliente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    onVolver: () -> Unit = {},
    onClienteConfirmado: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var dniQuery by remember { mutableStateOf("") }

    // Estados para controlar los diálogos flotantes
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var clienteSeleccionadoParaOpciones by remember { mutableStateOf<Any?>(null) } // Puede almacenar el objeto de cliente según la entidad

    val snackbarHostState = remember { SnackbarHostState() }

    // Si tu ViewModel provee una lista o usas la búsqueda por DNI del uiState
    // Aquí nos adaptamos al flujo original de tu uiState
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clientes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoAgregar = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de búsqueda por DNI (conecta con tu método original del ViewModel)
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
                label = { Text("Buscar por número de DNI (8 dígitos)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            // Renderizado según el uiState original
            when (val state = uiState) {
                is ClienteUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ClienteUiState.ClienteEncontrado -> {
                    // Tarjeta del cliente encontrado con opción a seleccionarlo o dar opciones
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clienteSeleccionadoParaOpciones = state.cliente },
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "Cliente Encontrado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(text = state.cliente.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                            Text(text = "DNI: ${state.cliente.dni} | Tel: ${state.cliente.telefono}", style = MaterialTheme.typography.bodyMedium)

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onClienteConfirmado(state.cliente.dni) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Seleccionar para Nueva OST")
                            }
                        }
                    }
                }
                is ClienteUiState.ClienteNoEncontrado -> {
                    // Si no se encuentra, mostramos un aviso directo para registrarlo con el diálogo flotante o formulario integrado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "No existe un cliente con DNI: ${state.dniBuscado}", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { mostrarDialogoAgregar = true }) {
                                Text("Registrar nuevo cliente con este DNI")
                            }
                        }
                    }
                }
                is ClienteUiState.RegistroExitoso -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("¡Cliente guardado exitosamente!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { viewModel.limpiarEstado(); dniQuery = "" }) {
                                Text("Realizar nueva búsqueda")
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingrese un DNI para buscar o pulse + para agregar un cliente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ==========================================
        // DIÁLOGO FLOTANTE: Registrar Nuevo Cliente
        // ==========================================
        if (mostrarDialogoAgregar) {
            DialogoFormularioCliente(
                titulo = "Registrar Nuevo Cliente",
                dniInicial = if (dniQuery.length == 8) dniQuery else "",
                onDismiss = { mostrarDialogoAgregar = false },
                onGuardar = { dni, nombre, direccion, sexo, telefono ->
                    viewModel.registrarCliente(
                        dni = dni,
                        nombre = nombre,
                        direccion = direccion,
                        sexo = sexo,
                        telefono = telefono
                    )
                    mostrarDialogoAgregar = false
                }
            )
        }
    }
}

/**
 * Componente reutilizable para el formulario flotante de agregar cliente.
 */
@Composable
fun DialogoFormularioCliente(
    titulo: String,
    dniInicial: String = "",
    nombreInicial: String = "",
    direccionInicial: String = "",
    sexoInicial: String = "Masculino",
    telefonoInicial: String = "",
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String) -> Unit
) {
    var dniInput by remember { mutableStateOf(dniInicial) }
    var nombreInput by remember { mutableStateOf(nombreInicial) }
    var direccionInput by remember { mutableStateOf(direccionInicial) }
    var sexoInput by remember { mutableStateOf(sexoInicial) }
    var telefonoInput by remember { mutableStateOf(telefonoInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dniInput,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) dniInput = it },
                    label = { Text("DNI (8 dígitos)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nombreInput,
                    onValueChange = { nombreInput = it },
                    label = { Text("Nombre Completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccionInput,
                    onValueChange = { direccionInput = it },
                    label = { Text("Dirección *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefonoInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) telefonoInput = it },
                    label = { Text("Teléfono / Celular *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Sexo", style = MaterialTheme.typography.labelMedium)
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dniInput.length == 8 && nombreInput.isNotBlank()) {
                        onGuardar(dniInput, nombreInput, direccionInput, sexoInput, telefonoInput)
                    }
                },
                enabled = dniInput.length == 8 && nombreInput.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
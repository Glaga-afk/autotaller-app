package com.example.autotallerapp.ui.screens.automovil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.Marca
import com.example.autotallerapp.domain.model.Modelo
import com.example.autotallerapp.ui.components.BotonPrincipal
import com.example.autotallerapp.ui.components.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAutomovilScreen(
    clienteId: String,
    placaEscaneada: String?,
    volver: () -> Unit,
    irAEscanerPlaca: () -> Unit,
    alRegistrar: () -> Unit,
    viewModel: RegistroAutomovilViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(placaEscaneada) {
        placaEscaneada?.let { viewModel.alCambiarPlaca(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { alRegistrar() }
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo automovil") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CampoTexto(
                    valor = estado.placa,
                    alCambiar = { viewModel.alCambiarPlaca(it.uppercase()) },
                    etiqueta = "Placa",
                    error = estado.errorPlaca,
                    habilitado = !estado.cargando,
                    soporte = "Ej: ABC-123",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = irAEscanerPlaca, enabled = !estado.cargando) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear placa")
                }
            }

            Spacer(Modifier.height(8.dp))

            SelectorMarca(
                marcas = estado.marcas,
                seleccionada = estado.marcaSeleccionada,
                error = estado.errorMarca,
                habilitado = !estado.cargando,
                alSeleccionar = viewModel::alSeleccionarMarca
            )

            Spacer(Modifier.height(8.dp))

            SelectorModelo(
                modelos = estado.modelos,
                seleccionado = estado.modeloSeleccionado,
                error = estado.errorModelo,
                habilitado = !estado.cargando && estado.marcaSeleccionada != null,
                alSeleccionar = viewModel::alSeleccionarModelo
            )

            Spacer(Modifier.height(8.dp))

            CampoTexto(
                valor = estado.anio,
                alCambiar = viewModel::alCambiarAnio,
                etiqueta = "Anio (opcional)",
                habilitado = !estado.cargando,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = estado.color,
                alCambiar = viewModel::alCambiarColor,
                etiqueta = "Color (opcional)",
                habilitado = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            BotonPrincipal(
                texto = "Registrar automovil",
                alPulsar = viewModel::alPulsarRegistrar,
                cargando = estado.cargando
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorMarca(
    marcas: List<Marca>,
    seleccionada: Marca?,
    error: String?,
    habilitado: Boolean,
    alSeleccionar: (Marca) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { if (habilitado) expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionada?.nombre.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            label = { Text("Marca") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            marcas.forEach { marca ->
                DropdownMenuItem(
                    text = { Text(marca.nombre) },
                    onClick = { alSeleccionar(marca); expandido = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorModelo(
    modelos: List<Modelo>,
    seleccionado: Modelo?,
    error: String?,
    habilitado: Boolean,
    alSeleccionar: (Modelo) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { if (habilitado) expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionado?.nombre.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            label = { Text("Modelo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            modelos.forEach { modelo ->
                DropdownMenuItem(
                    text = { Text(modelo.nombre) },
                    onClick = { alSeleccionar(modelo); expandido = false }
                )
            }
        }
    }
}
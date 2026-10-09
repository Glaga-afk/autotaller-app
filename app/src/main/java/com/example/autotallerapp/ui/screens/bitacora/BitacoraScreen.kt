package com.example.autotallerapp.ui.screens.bitacora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.ProblemaTecnico
import com.example.autotallerapp.domain.model.SistemaAutomovil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraScreen(
    volver: () -> Unit,
    viewModel: BitacoraViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Bitacora de problemas") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::alPulsarSincronizar, enabled = !estado.sincronizando) {
                        if (estado.sincronizando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = "Sincronizar bitacora")
                        }
                    }
                }
            )
        }
    ) { relleno ->
        Column(modifier = Modifier.fillMaxSize().padding(relleno)) {
            SearchBar(
                query = estado.palabraClave,
                onQueryChange = viewModel::alCambiarPalabraClave,
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Buscar por palabra clave...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::alAlternarFiltros) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtros")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}

            if (estado.filtrosVisibles) {
                FiltrosBitacora(
                    estado = estado,
                    alSeleccionarSistema = viewModel::alSeleccionarSistema,
                    alCambiarMarca = viewModel::alCambiarMarca,
                    alCambiarModelo = viewModel::alCambiarModelo,
                    alLimpiar = viewModel::limpiarFiltros
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                ContenidoBitacora(estado = estado, alSincronizar = viewModel::alPulsarSincronizar)
            }
        }
    }
}

@Composable
private fun FiltrosBitacora(
    estado: BitacoraUiState,
    alSeleccionarSistema: (SistemaAutomovil?) -> Unit,
    alCambiarMarca: (String) -> Unit,
    alCambiarModelo: (String) -> Unit,
    alLimpiar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Sistema", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SistemaAutomovil.entries) { sistema ->
                FilterChip(
                    selected = estado.sistemaSeleccionado == sistema,
                    onClick = {
                        alSeleccionarSistema(if (estado.sistemaSeleccionado == sistema) null else sistema)
                    },
                    label = { Text(sistema.etiqueta) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = estado.marca,
                onValueChange = alCambiarMarca,
                label = { Text("Marca") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = estado.modelo,
                onValueChange = alCambiarModelo,
                label = { Text("Modelo") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        if (estado.hayFiltrosActivos) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = alLimpiar) { Text("Limpiar filtros") }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun ContenidoBitacora(estado: BitacoraUiState, alSincronizar: () -> Unit) {
    when {
        estado.cargando && estado.resultados.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        estado.bitacoraVacia && !estado.hayFiltrosActivos -> {
            MensajeVacio(
                titulo = "Bitacora vacia",
                detalle = "Aun no se descargaron los casos registrados. Sincroniza para consultarlos sin conexion.",
                textoBoton = "Sincronizar ahora",
                alPulsarBoton = alSincronizar
            )
        }

        estado.resultados.isEmpty() -> {
            MensajeVacio(
                titulo = "No esta registrado",
                detalle = "Ningun problema de la bitacora coincide con esa busqueda. Si lo resolviste, pide que lo agreguen."
            )
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.resultados, key = { it.id }) { problema ->
                    TarjetaProblema(problema)
                }
            }
        }
    }
}

@Composable
private fun TarjetaProblema(problema: ProblemaTecnico) {
    var expandido by rememberSaveable(problema.id) { androidx.compose.runtime.mutableStateOf(false) }

    Card(onClick = { expandido = !expandido }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, enabled = false, label = { Text(problema.sistema.etiqueta) })
                Spacer(Modifier.width(8.dp))
                Text(
                    "${problema.marca} ${problema.modelo}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(problema.titulo, style = MaterialTheme.typography.titleMedium)

            if (expandido) {
                Spacer(Modifier.height(8.dp))
                Text("Problema", style = MaterialTheme.typography.labelLarge)
                Text(problema.descripcion, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Solucion",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(problema.solucion, style = MaterialTheme.typography.bodyMedium)
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Toca para ver la solucion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MensajeVacio(
    titulo: String,
    detalle: String,
    textoBoton: String? = null,
    alPulsarBoton: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ManageSearch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoBoton != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = alPulsarBoton) { Text(textoBoton) }
        }
    }
}
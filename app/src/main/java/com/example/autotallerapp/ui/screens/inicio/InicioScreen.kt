package com.example.autotallerapp.ui.screens.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.ui.navigation.Rutas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    irALogin: () -> Unit,
    irAModulo: (String) -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(estado.sesionCerrada) {
        if (estado.sesionCerrada && !estado.cargando) irALogin()
    }

    val usuario = estado.usuario

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesion") },
            text = { Text("Se cerrara tu sesion en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    viewModel.cerrarSesion()
                }) { Text("Cerrar sesion") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola,", style = MaterialTheme.typography.bodySmall)
                        Text(
                            usuario?.nombre?.ifBlank { "Usuario" } ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarSalida = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesion"
                        )
                    }
                }
            )
        },
        bottomBar = { usuario?.let { BarraInferior(it, irAModulo) } }
    ) { relleno ->
        if (estado.cargando || usuario == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(relleno),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp)
        ) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(usuario.rol.etiqueta) },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MenuPorRol.opciones(usuario.rol)) { opcion ->
                    TarjetaModulo(opcion = opcion, alPulsar = { irAModulo(opcion.ruta) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaModulo(opcion: OpcionMenu, alPulsar: () -> Unit) {
    Card(
        onClick = alPulsar,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.25f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = opcion.icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = opcion.titulo,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BarraInferior(usuario: Usuario, irAModulo: (String) -> Unit) {
    NavigationBar {
        MenuPorRol.opcionesBarraInferior(usuario.rol).forEachIndexed { indice, opcion ->
            NavigationBarItem(
                selected = indice == 0,
                onClick = { if (opcion.ruta != Rutas.INICIO) irAModulo(opcion.ruta) },
                icon = { Icon(opcion.icono, contentDescription = null) },
                label = { Text(opcion.titulo) }
            )
        }
    }
}

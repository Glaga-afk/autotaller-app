package com.example.autotallerapp.ui.screens.indicadores

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autotallerapp.domain.model.EstadoOrden
import com.example.autotallerapp.ui.components.BarraSuperior
import com.example.autotallerapp.ui.theme.PlayfairDisplay
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicadoresScreen(
    alVolver: () -> Unit,
    viewModel: IndicadoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Dashboard de Indicadores",
                alVolver = alVolver,
                fuenteTitulo = PlayfairDisplay,
                iconoPantalla = Icons.Outlined.PieChart,
                acciones = {
                    IconButton(onClick = { viewModel.cargarIndicadores() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar indicadores")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.cargando && uiState.totalOrdenes == 0) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.totalOrdenes == 0) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filtros de Tiempo (Histórico, Este Mes, Esta Semana)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FiltroTiempo.entries.forEach { filtro ->
                                FilterChip(
                                    selected = uiState.filtroSeleccionado == filtro,
                                    onClick = { viewModel.cambiarFiltro(filtro) },
                                    label = {
                                        Text(
                                            text = when (filtro) {
                                                FiltroTiempo.HISTORICO -> "Histórico"
                                                FiltroTiempo.ESTE_MES -> "Este Mes"
                                                FiltroTiempo.ESTA_SEMANA -> "Esta Semana"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Resumen general
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total de Órdenes de Servicio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${uiState.totalOrdenes}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Icon(
                                    Icons.Default.Assessment,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Gráfico 1: Órdenes por Estado (Gráfico de Dona - PieChart)
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Órdenes por Estado (Dona)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.totalOrdenes == 0) {
                                    Text("No hay datos disponibles para el periodo.", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        factory = { context ->
                                            PieChart(context).apply {
                                                description.isEnabled = false
                                                isDrawHoleEnabled = true
                                                setHoleRadius(45f)
                                                setTransparentCircleRadius(50f)
                                                legend.isEnabled = true
                                            }
                                        },
                                        update = { chart ->
                                            val entries = mutableListOf<PieEntry>()
                                            EstadoOrden.entries.forEach { estado ->
                                                val count = uiState.ordenesPorEstado[estado] ?: 0
                                                if (count > 0) {
                                                    entries.add(PieEntry(count.toFloat(), estado.name))
                                                }
                                            }

                                            val dataSet = PieDataSet(entries, "Estados").apply {
                                                colors = ColorTemplate.MATERIAL_COLORS.toList()
                                                valueTextSize = 12f
                                                valueTextColor = Color.BLACK
                                            }

                                            chart.data = PieData(dataSet)
                                            chart.invalidate()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Gráfico 2: Fallas más frecuentes (Gráfico de Pastel - PieChart)
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Fallas Más Frecuentes (Pastel)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.fallasFrecuentes.isEmpty()) {
                                    Text("No hay suficientes datos de fallas registradas.", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        factory = { context ->
                                            PieChart(context).apply {
                                                description.isEnabled = false
                                                isDrawHoleEnabled = false
                                                legend.isEnabled = true
                                            }
                                        },
                                        update = { chart ->
                                            val entries = uiState.fallasFrecuentes.map { (falla, count) ->
                                                PieEntry(count.toFloat(), falla)
                                            }

                                            val dataSet = PieDataSet(entries, "Fallas").apply {
                                                colors = ColorTemplate.COLORFUL_COLORS.toList()
                                                valueTextSize = 12f
                                                valueTextColor = Color.BLACK
                                            }

                                            chart.data = PieData(dataSet)
                                            chart.invalidate()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Gráfico 3: Productividad / Órdenes por Estado (Gráfico de Barras - BarChart)
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Resumen Operativo (Barras)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.totalOrdenes == 0) {
                                    Text("No hay datos disponibles.", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        factory = { context ->
                                            BarChart(context).apply {
                                                description.isEnabled = false
                                                legend.isEnabled = false
                                                xAxis.granularity = 1f
                                                axisRight.isEnabled = false
                                            }
                                        },
                                        update = { chart ->
                                            val entries = mutableListOf<BarEntry>()
                                            EstadoOrden.entries.forEachIndexed { index, estado ->
                                                val count = uiState.ordenesPorEstado[estado] ?: 0
                                                entries.add(BarEntry(index.toFloat(), count.toFloat()))
                                            }

                                            val dataSet = BarDataSet(entries, "Órdenes").apply {
                                                colors = ColorTemplate.VORDIPLOM_COLORS.toList()
                                                valueTextSize = 12f
                                            }

                                            chart.data = BarData(dataSet)
                                            chart.invalidate()
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
}

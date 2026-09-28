package com.example.autotallerapp.ui.screens.escaner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.autotallerapp.ui.ocr.EscanerPlacaAnalyzer
import java.util.concurrent.Executors
import androidx.compose.material3.Surface
import com.example.autotallerapp.ui.components.BotonPrincipal
import android.os.SystemClock
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import kotlinx.coroutines.delay
import com.example.autotallerapp.ui.ocr.EstabilizadorPlaca
import com.example.autotallerapp.ui.theme.VerdeExito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerPlacaScreen(
    volver: () -> Unit,
    alDetectarPlaca: (String) -> Unit
) {
    val contexto = LocalContext.current
    val estabilizador = remember { EstabilizadorPlaca() }
    var estadoLectura by remember { mutableStateOf(EstabilizadorPlaca.Estado()) }
    var placaConfirmada by remember { mutableStateOf<String?>(null) }
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    LaunchedEffect(Unit) {
        if (!tienePermiso) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // Revisa el estado de la lectura cada 80 ms hasta que la placa quede confirmada
    LaunchedEffect(placaConfirmada) {
        while (placaConfirmada == null) {
            val estado = estabilizador.evaluar(SystemClock.elapsedRealtime())
            estadoLectura = estado
            if (estado.confirmada) placaConfirmada = estado.candidata
            delay(80)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear placa") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        Box(modifier = Modifier.fillMaxSize().padding(relleno)) {
            if (tienePermiso) {
                VisorCamara(alDetectarPlaca = { placa ->
                    if (placaConfirmada == null) {
                        estabilizador.registrar(placa, SystemClock.elapsedRealtime())
                    }
                })
                MarcoGuiaPlaca(
                    confirmada = placaConfirmada != null,
                    modifier = Modifier.align(Alignment.Center)
                )
                PanelConfirmacion(
                    estado = estadoLectura,
                    placaConfirmada = placaConfirmada,
                    alConfirmar = { placaConfirmada?.let(alDetectarPlaca) },
                    alReintentar = {
                        estabilizador.reiniciar()
                        estadoLectura = EstabilizadorPlaca.Estado()
                        placaConfirmada = null
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Se necesita permiso de camara para escanear la placa",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
private fun VisorCamara(alDetectarPlaca: (String) -> Unit) {
    val cicloDeVida = LocalLifecycleOwner.current
    val ejecutorAnalisis = remember { Executors.newSingleThreadExecutor() }
    val callbackActualizado = rememberUpdatedState(alDetectarPlaca)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val vistaPrevisualizacion = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val proveedorCamaraFuturo = ProcessCameraProvider.getInstance(ctx)

            proveedorCamaraFuturo.addListener({
                val proveedorCamara = proveedorCamaraFuturo.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(vistaPrevisualizacion.surfaceProvider)
                }

                val resolucionAnalisis = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1280, 960),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val analisisImagen = ImageAnalysis.Builder()
                    .setResolutionSelector(resolucionAnalisis)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            ejecutorAnalisis,
                            EscanerPlacaAnalyzer { placa -> callbackActualizado.value(placa) }
                        )
                    }

                try {
                    proveedorCamara.unbindAll()
                    proveedorCamara.bindToLifecycle(
                        cicloDeVida,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analisisImagen
                    )
                } catch (e: Exception) {
                    android.util.Log.e("EscanerPlaca", "No se pudo abrir la camara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            vistaPrevisualizacion
        }
    )

    DisposableEffect(Unit) {
        onDispose { ejecutorAnalisis.shutdown() }
    }
}

@Composable
private fun MarcoGuiaPlaca(confirmada: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(3f)
                .border(
                    width = 3.dp,
                    color = if (confirmada) VerdeExito else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Coloca la placa en el recuadro y mantenla 2 segundos",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PanelConfirmacion(
    estado: EstabilizadorPlaca.Estado,
    placaConfirmada: String?,
    alConfirmar: () -> Unit,
    alReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titulo = when {
        placaConfirmada != null -> "Placa confirmada"
        estado.candidata != null -> "Manten quieta la camara..."
        else -> "Buscando placa..."
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = placaConfirmada ?: estado.candidata ?: "---",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))

            if (placaConfirmada == null) {
                LinearProgressIndicator(
                    progress = { estado.progreso },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            BotonPrincipal(
                texto = "Usar esta placa",
                alPulsar = alConfirmar,
                habilitado = placaConfirmada != null
            )
            if (placaConfirmada != null) {
                TextButton(onClick = alReintentar) { Text("Escanear de nuevo") }
            }
        }
    }
}
package com.example.autotallerapp.ui.screens.orden

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.autotallerapp.data.device.ProcesadorFoto
import com.example.autotallerapp.data.local.GestorArchivosOrden
import com.example.autotallerapp.domain.model.TipoArchivo
import com.example.autotallerapp.ui.components.BotonPrincipal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Toma una foto del automovil con CameraX (caso de uso ImageCapture).
 * La foto se gira, reduce y comprime antes de guardarse, y su ruta
 * se devuelve a la pantalla de la orden.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturaFotoScreen(
    volver: () -> Unit,
    alCapturar: (String) -> Unit
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var capturando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
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

    val capturaImagen = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    fun tomarFoto() {
        capturando = true
        error = null
        capturaImagen.takePicture(
            ContextCompat.getMainExecutor(contexto),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imagen: ImageProxy) {
                    alcance.launch {
                        val resultado = withContext(Dispatchers.Default) {
                            runCatching {
                                imagen.use {
                                    val destino = GestorArchivosOrden.crearArchivoCaptura(
                                        contexto, "foto", TipoArchivo.FOTO.extension
                                    )
                                    val bitmap = it.toBitmap()
                                    ProcesadorFoto.guardar(bitmap, it.imageInfo.rotationDegrees, destino)
                                    bitmap.recycle()
                                    destino.absolutePath
                                }
                            }
                        }
                        capturando = false
                        resultado
                            .onSuccess(alCapturar)
                            .onFailure { error = "No se pudo guardar la foto. Intentalo de nuevo" }
                    }
                }

                override fun onError(excepcion: ImageCaptureException) {
                    Log.e("CapturaFoto", "Error al capturar", excepcion)
                    capturando = false
                    error = "No se pudo tomar la foto. Intentalo de nuevo"
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foto del automovil") },
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
                VisorCaptura(capturaImagen = capturaImagen)

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            error ?: "Encuadra la zona de la falla",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (error != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        BotonPrincipal(
                            texto = "Tomar foto",
                            alPulsar = ::tomarFoto,
                            cargando = capturando
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Se necesita permiso de camara para tomar la foto",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun VisorCaptura(capturaImagen: ImageCapture) {
    val cicloDeVida = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val vistaPrevia = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val proveedorFuturo = ProcessCameraProvider.getInstance(ctx)

            proveedorFuturo.addListener({
                val proveedor = proveedorFuturo.get()
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(vistaPrevia.surfaceProvider) }

                try {
                    proveedor.unbindAll()
                    proveedor.bindToLifecycle(
                        cicloDeVida,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capturaImagen
                    )
                } catch (e: Exception) {
                    Log.e("CapturaFoto", "No se pudo abrir la camara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            vistaPrevia
        }
    )
}

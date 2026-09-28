package com.example.autotallerapp.ui.ocr

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.autotallerapp.core.PlacaValidador

class EscanerPlacaAnalyzer(
    private val alDetectarPlaca: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var procesando = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || procesando) {
            imageProxy.close()
            return
        }

        procesando = true
        val imagenEntrada = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        reconocedor.process(imagenEntrada)
            .addOnSuccessListener { texto ->
                val candidatos = texto.textBlocks.flatMap { bloque -> bloque.lines.map { it.text } }
                PlacaValidador.buscarEnLineas(candidatos)?.let(alDetectarPlaca)
            }
            .addOnCompleteListener {
                procesando = false
                imageProxy.close()
            }
    }

}
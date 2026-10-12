package com.example.autotallerapp.ui.ocr

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.autotallerapp.core.LineaOcr
import com.example.autotallerapp.core.PlacaValidador
class EscanerPlacaAnalyzer(
    private val alDetectarPlaca: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @Volatile private var procesando = false
    @Volatile private var anchoVista = 0
    @Volatile private var altoVista = 0

    /** La pantalla avisa el tamano del visor para poder ubicar el recuadro dentro de la imagen */
    fun actualizarTamanoVista(ancho: Int, alto: Int) {
        anchoVista = ancho
        altoVista = alto
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || procesando) {
            imageProxy.close()
            return
        }

        val rotacion = imageProxy.imageInfo.rotationDegrees
        val girada = rotacion == 90 || rotacion == 270
        val anchoImagen = if (girada) imageProxy.height else imageProxy.width
        val altoImagen = if (girada) imageProxy.width else imageProxy.height
        val region = RegionGuiaPlaca.enImagen(anchoVista, altoVista, anchoImagen, altoImagen)

        if (region == null) {
            imageProxy.close()
            return
        }

        procesando = true
        val imagenEntrada = InputImage.fromMediaImage(mediaImage, rotacion)

        reconocedor.process(imagenEntrada)
            .addOnSuccessListener { texto ->
                val lineasEnRecuadro = texto.textBlocks
                    .flatMap { it.lines }
                    .mapNotNull { linea ->
                        val caja = linea.boundingBox ?: return@mapNotNull null
                        if (!estaDentro(caja, region)) return@mapNotNull null
                        LineaOcr(texto = linea.text, tamano = caja.width() * caja.height())
                    }
                PlacaValidador.buscarMasGrande(lineasEnRecuadro)?.let(alDetectarPlaca)
            }
            .addOnCompleteListener {
                procesando = false
                imageProxy.close()
            }
    }

    /** Cuenta como "dentro" si al menos el 80% de la caja del texto cae en el recuadro */
    private fun estaDentro(caja: Rect, region: RectF): Boolean {
        val areaCaja = caja.width() * caja.height().toFloat()
        if (areaCaja <= 0f) return false

        val ancho = minOf(caja.right.toFloat(), region.right) - maxOf(caja.left.toFloat(), region.left)
        val alto = minOf(caja.bottom.toFloat(), region.bottom) - maxOf(caja.top.toFloat(), region.top)
        if (ancho <= 0f || alto <= 0f) return false

        return (ancho * alto) / areaCaja >= 0.8f
    }

}
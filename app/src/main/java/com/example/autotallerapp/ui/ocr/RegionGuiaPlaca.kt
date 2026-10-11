package com.example.autotallerapp.ui.ocr

import android.graphics.RectF

object RegionGuiaPlaca {
    const val ANCHO_FRACCION = 0.85f
    const val RELACION_ASPECTO = 3f

    /**
     * Devuelve el recuadro guia en coordenadas de la imagen analizada (ya rotada).
     * PreviewView usa FILL_CENTER: la imagen se escala hasta cubrir la vista y se recorta el sobrante.
     */
    fun enImagen(anchoVista: Int, altoVista: Int, anchoImagen: Int, altoImagen: Int): RectF? {
        if (anchoVista <= 0 || altoVista <= 0 || anchoImagen <= 0 || altoImagen <= 0) return null

        val anchoMarco = anchoVista * ANCHO_FRACCION
        val altoMarco = anchoMarco / RELACION_ASPECTO
        val izquierda = (anchoVista - anchoMarco) / 2f
        val arriba = (altoVista - altoMarco) / 2f

        val escala = maxOf(anchoVista / anchoImagen.toFloat(), altoVista / altoImagen.toFloat())
        val desplazX = (anchoVista - anchoImagen * escala) / 2f
        val desplazY = (altoVista - altoImagen * escala) / 2f

        return RectF(
            (izquierda - desplazX) / escala,
            (arriba - desplazY) / escala,
            (izquierda + anchoMarco - desplazX) / escala,
            (arriba + altoMarco - desplazY) / escala
        )
    }
}
package com.example.autotallerapp.domain.model

/** Foto en Base64 (el mismo formato con que se guarda en Firestore y que pide Gemini). */
data class FotoEvidencia(
    val base64: String,
    val mime: String
)

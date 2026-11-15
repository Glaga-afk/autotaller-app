package com.example.autotallerapp.data.remote.ia

import com.google.gson.annotations.SerializedName

// ----- Solicitud -----

data class GeminiSolicitud(
    @SerializedName("systemInstruction") val instruccionSistema: GeminiContenido? = null,
    @SerializedName("contents") val contenidos: List<GeminiContenido> = emptyList(),
    @SerializedName("generationConfig") val configuracion: GeminiConfiguracion? = null
)

data class GeminiContenido(
    @SerializedName("role") val rol: String? = null,
    @SerializedName("parts") val partes: List<GeminiParte>? = null
)

data class GeminiParte(
    @SerializedName("text") val texto: String? = null,
    @SerializedName("inlineData") val datosEnLinea: GeminiDatosEnLinea? = null
)

data class GeminiDatosEnLinea(
    @SerializedName("mimeType") val tipoMime: String,
    @SerializedName("data") val datos: String
)

data class GeminiConfiguracion(
    @SerializedName("temperature") val temperatura: Double? = null,
    @SerializedName("responseMimeType") val tipoRespuesta: String? = null,
    @SerializedName("responseSchema") val esquemaRespuesta: Map<String, Any>? = null
)

// ----- Respuesta -----

data class GeminiRespuesta(
    @SerializedName("candidates") val candidatos: List<GeminiCandidato>? = null,
    @SerializedName("promptFeedback") val retroalimentacion: GeminiRetroalimentacion? = null
)

data class GeminiCandidato(
    @SerializedName("content") val contenido: GeminiContenido? = null,
    @SerializedName("finishReason") val motivoFin: String? = null
)

data class GeminiRetroalimentacion(
    @SerializedName("blockReason") val motivoBloqueo: String? = null
)

/** El JSON que el modelo devuelve dentro del texto, segun el esquema que le pedimos. */
data class SugerenciaIaDto(
    @SerializedName("sistema") val sistema: String? = null,
    @SerializedName("causaProbable") val causaProbable: String? = null,
    @SerializedName("urgencia") val urgencia: String? = null,
    @SerializedName("palabrasClave") val palabrasClave: List<String>? = null
)
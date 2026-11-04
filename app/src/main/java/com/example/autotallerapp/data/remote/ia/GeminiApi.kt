package com.example.autotallerapp.data.remote.ia

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApi {

    /** La clave va por cabecera, asi nunca aparece en la URL ni en los logs. */
    @POST("v1beta/models/{modelo}:generateContent")
    suspend fun generarContenido(
        @Path("modelo") modelo: String,
        @Header("x-goog-api-key") claveApi: String,
        @Body solicitud: GeminiSolicitud
    ): GeminiRespuesta
}
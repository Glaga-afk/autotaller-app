package com.example.autotallerapp.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.autotallerapp.R

class GoogleAuthCliente(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun obtenerIdToken(): String {
        val opcionGoogle = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val respuesta = credentialManager.getCredential(context, solicitud)
        val credencial = respuesta.credential

        if (credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("Credencial de Google no valida")
        }
        return GoogleIdTokenCredential.createFrom(credencial.data).idToken
    }
}

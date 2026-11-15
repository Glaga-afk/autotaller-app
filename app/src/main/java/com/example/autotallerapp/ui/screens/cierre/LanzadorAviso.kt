package com.example.autotallerapp.ui.screens.cierre

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * HU08: abre WhatsApp o la app de mensajes con el aviso ya escrito.
 * No envia nada por su cuenta (no necesita permisos): el supervisor revisa y pulsa "Enviar".
 */
object LanzadorAviso {

    /** telefono en formato 51987654321. Devuelve false si no hay app que lo abra. */
    fun abrirWhatsApp(contexto: Context, telefono: String, mensaje: String): Boolean {
        // Enlace oficial "click to chat": abre WhatsApp o, si no esta instalado, el navegador
        val enlace = Uri.parse("https://wa.me/$telefono?text=${Uri.encode(mensaje)}")
        return abrir(contexto, Intent(Intent.ACTION_VIEW, enlace))
    }

    fun abrirSms(contexto: Context, telefono: String, mensaje: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+$telefono"))
            .putExtra("sms_body", mensaje)
        return abrir(contexto, intent)
    }

    private fun abrir(contexto: Context, intent: Intent): Boolean = try {
        contexto.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

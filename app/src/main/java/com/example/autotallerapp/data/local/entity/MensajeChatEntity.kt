package com.example.autotallerapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.model.RolChat

/** Un mensaje del chat guardado en el celular. La lista de fuentes usa el convertidor "|" de Room. */
@Entity(tableName = "mensajes_chat", indices = [Index("conversacionId")])
data class MensajeChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversacionId: String,
    val rol: String,
    val texto: String,
    val fuentes: List<String>,
    val creadoEn: Long
) {
    fun aDominio(): MensajeChat = MensajeChat(
        id = id,
        rol = RolChat.desde(rol),
        texto = texto,
        fuentes = fuentes,
        creadoEn = creadoEn
    )

    companion object {
        fun desdeDominio(conversacionId: String, mensaje: MensajeChat): MensajeChatEntity =
            MensajeChatEntity(
                id = mensaje.id,
                conversacionId = conversacionId,
                rol = mensaje.rol.name,
                texto = mensaje.texto,
                // El convertidor separa por "|": se evita que un titulo lo traiga
                fuentes = mensaje.fuentes.map { it.replace("|", "/") },
                creadoEn = mensaje.creadoEn
            )
    }
}

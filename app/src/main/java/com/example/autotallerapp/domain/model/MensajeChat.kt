package com.example.autotallerapp.domain.model

/** Conversacion usada cuando el tecnico no elige ninguna orden. */
const val CONVERSACION_GENERAL = "general"

/** Un mensaje del chat. Los del asistente guardan los casos de la bitacora que consulto. */
data class MensajeChat(
    val id: Long = 0,
    val rol: RolChat,
    val texto: String,
    val fuentes: List<String> = emptyList(),
    val creadoEn: Long = System.currentTimeMillis()
)

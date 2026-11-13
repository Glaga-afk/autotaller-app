package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.model.OrdenServicio
import com.example.autotallerapp.domain.model.RolChat
import com.example.autotallerapp.domain.repository.AsistenteIaRepository
import com.example.autotallerapp.domain.repository.HistorialChatRepository
import javax.inject.Inject

/**
 * Una consulta completa al asistente:
 * guarda la pregunta, busca casos parecidos en la bitacora, consulta a la IA
 * y guarda la respuesta junto con los casos en que se baso.
 */
class EnviarConsultaAsistenteUseCase @Inject constructor(
    private val historial: HistorialChatRepository,
    private val asistente: AsistenteIaRepository,
    private val buscarCasosSimilares: BuscarCasosSimilaresUseCase
) {
    suspend operator fun invoke(
        conversacionId: String,
        orden: OrdenServicio?,
        consulta: String,
        historialPrevio: List<MensajeChat>,
        consultaYaGuardada: Boolean = false
    ): Resultado<Unit> {
        val texto = consulta.trim()
        if (texto.length < MIN_CARACTERES) return Resultado.Error("Escribe o dicta tu consulta")
        if (texto.length > MAX_CARACTERES) {
            return Resultado.Error("La consulta es muy larga (maximo $MAX_CARACTERES caracteres)")
        }

        // En un reintento la pregunta ya esta en el historial: no se duplica
        if (!consultaYaGuardada) {
            val guardada = historial.guardar(
                conversacionId,
                MensajeChat(rol = RolChat.USUARIO, texto = texto)
            )
            if (guardada is Resultado.Error) return guardada
        }

        val casos = buscarCasosSimilares(
            consulta = listOfNotNull(texto, orden?.falla).joinToString(" "),
            marca = orden?.marca.orEmpty(),
            modelo = orden?.modelo.orEmpty()
        )

        return when (val respuesta = asistente.responder(orden, casos, historialPrevio.takeLast(MAX_HISTORIAL), texto)) {
            is Resultado.Exito -> historial.guardar(
                conversacionId,
                MensajeChat(rol = RolChat.ASISTENTE, texto = respuesta.dato, fuentes = casos.map { it.titulo })
            )
            is Resultado.Error -> Resultado.Error(respuesta.mensaje)
        }
    }

    companion object {
        const val MIN_CARACTERES = 2
        const val MAX_CARACTERES = 500
        const val MAX_HISTORIAL = 10
    }
}
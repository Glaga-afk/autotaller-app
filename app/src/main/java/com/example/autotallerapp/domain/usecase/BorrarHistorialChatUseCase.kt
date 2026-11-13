package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.repository.HistorialChatRepository
import javax.inject.Inject

class BorrarHistorialChatUseCase @Inject constructor(
    private val repository: HistorialChatRepository
) {
    suspend operator fun invoke(conversacionId: String): Resultado<Unit> =
        repository.borrar(conversacionId)
}
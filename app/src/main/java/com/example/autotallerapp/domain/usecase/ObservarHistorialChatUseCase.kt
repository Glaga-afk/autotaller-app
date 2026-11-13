package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.domain.model.MensajeChat
import com.example.autotallerapp.domain.repository.HistorialChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarHistorialChatUseCase @Inject constructor(
    private val repository: HistorialChatRepository
) {
    operator fun invoke(conversacionId: String): Flow<List<MensajeChat>> =
        repository.observar(conversacionId)
}
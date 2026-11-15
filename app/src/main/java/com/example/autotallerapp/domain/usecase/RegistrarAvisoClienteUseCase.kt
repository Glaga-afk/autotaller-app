package com.example.autotallerapp.domain.usecase

import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.domain.model.CanalAviso
import com.example.autotallerapp.domain.repository.OrdenRepository
import javax.inject.Inject

/** HU08: deja constancia en la OST de que se aviso al cliente y por que canal. */
class RegistrarAvisoClienteUseCase @Inject constructor(
    private val repository: OrdenRepository
) {
    suspend operator fun invoke(idOrden: String, canal: CanalAviso): Resultado<Unit> =
        repository.registrarAvisoCliente(idOrden, canal)
}

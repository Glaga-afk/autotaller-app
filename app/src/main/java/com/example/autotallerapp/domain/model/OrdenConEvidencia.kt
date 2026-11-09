package com.example.autotallerapp.domain.model

data class OrdenConEvidencia(
    val orden: OrdenServicio,
    val fotos: List<FotoEvidencia>
)

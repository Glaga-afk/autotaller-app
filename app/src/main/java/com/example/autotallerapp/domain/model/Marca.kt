package com.example.autotallerapp.domain.model

data class Marca(
    val id: Int = 0,
    val nombre: String = ""
)

data class Modelo(
    val id: Int = 0,
    val marcaId: Int = 0,
    val nombre: String = ""
)
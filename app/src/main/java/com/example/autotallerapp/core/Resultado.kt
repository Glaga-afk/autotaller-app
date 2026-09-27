package com.example.autotallerapp.core

sealed interface Resultado<out T> {
    data class Exito<T>(val dato: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}

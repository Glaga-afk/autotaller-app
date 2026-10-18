package com.example.autotallerapp.data.local

import androidx.room.TypeConverter

/** Room no guarda listas directamente: las convierte a un texto separado por "|". */
class Convertidores {

    @TypeConverter
    fun desdeLista(lista: List<String>): String = lista.joinToString(SEPARADOR)

    @TypeConverter
    fun aLista(texto: String): List<String> =
        if (texto.isEmpty()) emptyList() else texto.split(SEPARADOR)

    private companion object {
        const val SEPARADOR = "|"
    }
}

package com.example.autotallerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.autotallerapp.data.local.entity.OrdenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(orden: OrdenEntity)

    /** Ordenes que todavia no llegan a Firestore, de la mas antigua a la mas reciente. */
    @Query("SELECT * FROM ordenes WHERE estadoEnvio != 'ENVIADA' ORDER BY fechaRegistro ASC")
    suspend fun obtenerPorEnviar(): List<OrdenEntity>

    @Query(
        "UPDATE ordenes SET estadoEnvio = 'ENVIADA', numero = :numero, ultimoError = NULL " +
            "WHERE idLocal = :idLocal"
    )
    suspend fun marcarEnviada(idLocal: String, numero: Long)

    @Query(
        "UPDATE ordenes SET estadoEnvio = 'ERROR', intentos = intentos + 1, ultimoError = :error " +
            "WHERE idLocal = :idLocal"
    )
    suspend fun marcarError(idLocal: String, error: String)

    /** Flow: Room avisa cada vez que la fila cambia (por ejemplo, cuando recibe su numero). */
    @Query("SELECT * FROM ordenes WHERE idLocal = :idLocal LIMIT 1")
    fun observar(idLocal: String): Flow<OrdenEntity?>
}

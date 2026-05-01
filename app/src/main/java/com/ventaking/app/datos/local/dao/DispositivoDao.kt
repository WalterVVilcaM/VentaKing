package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DispositivoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(dispositivo: DispositivoEntity)

    @Update
    suspend fun actualizar(dispositivo: DispositivoEntity)

    @Query("SELECT * FROM dispositivo LIMIT 1")
    suspend fun obtenerActual(): DispositivoEntity?

    @Query("SELECT * FROM dispositivo LIMIT 1")
    fun observarActual(): Flow<DispositivoEntity?>

    @Query("""
        UPDATE dispositivo
        SET nombreDispositivo = :nombreDispositivo,
            actualizadoEn = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun actualizarNombre(
        id: String,
        nombreDispositivo: String?,
        actualizadoEn: Long
    )

    @Query("""
        UPDATE dispositivo
        SET ultimoRespaldoEn = :ultimoRespaldoEn,
            actualizadoEn = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun actualizarUltimoRespaldo(
        id: String,
        ultimoRespaldoEn: Long,
        actualizadoEn: Long
    )
}
package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorteDiarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(corte: CorteDiarioEntity)

    @Update
    suspend fun actualizar(corte: CorteDiarioEntity)

    @Query("SELECT * FROM cortes_diarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): CorteDiarioEntity?

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE negocioId = :negocioId
        AND fechaCorte = :fechaCorte
        AND dispositivoId = :dispositivoId
        LIMIT 1
    """)
    suspend fun obtenerPorNegocioFechaDispositivo(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): CorteDiarioEntity?

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE fechaCorte = :fechaCorte
        ORDER BY creadoEn DESC
    """)
    fun observarPorFecha(fechaCorte: String): Flow<List<CorteDiarioEntity>>

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE fechaCorte = :fechaCorte
        AND negocioId = :negocioId
        ORDER BY creadoEn DESC
    """)
    fun observarPorFechaYNegocio(
        fechaCorte: String,
        negocioId: String
    ): Flow<List<CorteDiarioEntity>>

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE fechaCorte = :fechaCorte
        AND negocioId = :negocioId
        AND dispositivoId = :dispositivoId
        ORDER BY creadoEn DESC
    """)
    fun observarPorFechaNegocioYDispositivo(
        fechaCorte: String,
        negocioId: String,
        dispositivoId: String
    ): Flow<List<CorteDiarioEntity>>

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE syncEstado IN (:estados)
        ORDER BY creadoEn ASC
    """)
    fun observarPorEstadosSync(estados: List<String>): Flow<List<CorteDiarioEntity>>

    @Query("""
        UPDATE cortes_diarios
        SET syncEstado = :syncEstado,
            sincronizadoEn = :sincronizadoEn,
            mensajeError = :mensajeError
        WHERE id = :corteId
    """)
    suspend fun actualizarSyncEstado(
        corteId: String,
        syncEstado: String,
        sincronizadoEn: Long?,
        mensajeError: String?
    )

    @Query("""
        SELECT * FROM cortes_diarios
        WHERE syncEstado IN (:estados)
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerListaPorEstadosSync(estados: List<String>): List<CorteDiarioEntity>
}
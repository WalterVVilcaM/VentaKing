package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialVentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(historial: HistorialVentaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(historiales: List<HistorialVentaEntity>)

    @Query("""
        SELECT * FROM historial_ventas
        WHERE ventaId = :ventaId
        ORDER BY creadoEn ASC
    """)
    fun observarPorVenta(ventaId: String): Flow<List<HistorialVentaEntity>>

    @Query("""
        SELECT * FROM historial_ventas
        WHERE grupoVentaId = :grupoVentaId
        ORDER BY creadoEn ASC
    """)
    fun observarPorGrupoVenta(grupoVentaId: String): Flow<List<HistorialVentaEntity>>

    @Query("""
        SELECT * FROM historial_ventas
        WHERE negocioId = :negocioId
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocio(negocioId: String): Flow<List<HistorialVentaEntity>>

    @Query("""
        SELECT * FROM historial_ventas
        WHERE negocioId = :negocioId
        AND creadoEn BETWEEN :desde AND :hasta
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioYRango(
        negocioId: String,
        desde: Long,
        hasta: Long
    ): Flow<List<HistorialVentaEntity>>

    @Query("""
        SELECT * FROM historial_ventas
        WHERE negocioId = :negocioId
        AND dispositivoId = :dispositivoId
        AND creadoEn BETWEEN :desde AND :hasta
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioDispositivoYRango(
        negocioId: String,
        dispositivoId: String,
        desde: Long,
        hasta: Long
    ): Flow<List<HistorialVentaEntity>>

    @Query("""
        SELECT * FROM historial_ventas
        WHERE syncEstado = :syncEstado
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerPorSyncEstado(syncEstado: String): List<HistorialVentaEntity>
}
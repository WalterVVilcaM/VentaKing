package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.VentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(venta: VentaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(ventas: List<VentaEntity>)

    @Update
    suspend fun actualizar(venta: VentaEntity)

    @Query("SELECT * FROM ventas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): VentaEntity?

    @Query("""
        SELECT * FROM ventas
        WHERE grupoVentaId = :grupoVentaId
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerPorGrupoVentaId(grupoVentaId: String): List<VentaEntity>

    @Query("""
        SELECT * FROM ventas
        WHERE corteId = :corteId
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerPorCorteId(corteId: String): List<VentaEntity>

    @Query("""
        SELECT * FROM ventas
        WHERE corteId = :corteId
        AND estado = :estado
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerPorCorteIdYEstado(
        corteId: String,
        estado: String
    ): List<VentaEntity>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<VentaEntity>>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND estado = :estado
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioFechaYEstado(
        negocioId: String,
        fechaVenta: String,
        estado: String
    ): Flow<List<VentaEntity>>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND dispositivoId = :dispositivoId
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioFechaYDispositivo(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String
    ): Flow<List<VentaEntity>>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND dispositivoId = :dispositivoId
        AND estado = :estado
        ORDER BY creadoEn DESC
    """)
    fun observarPorNegocioFechaDispositivoYEstado(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String,
        estado: String
    ): Flow<List<VentaEntity>>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND dispositivoId = :dispositivoId
        AND estado = :estado
        AND corteId IS NULL
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerVentasAbiertasParaCorte(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String,
        estado: String
    ): List<VentaEntity>

    @Query("""
        SELECT * FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND dispositivoId = :dispositivoId
        AND estado = :estado
        AND corteId IS NULL
        ORDER BY creadoEn ASC
    """)
    fun observarVentasAbiertasParaCorte(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String,
        estado: String
    ): Flow<List<VentaEntity>>

    @Query("""
        SELECT COALESCE(SUM(totalCentavos), 0) FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND estado = :estado
    """)
    fun observarTotalPorNegocioFechaYEstado(
        negocioId: String,
        fechaVenta: String,
        estado: String
    ): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(cantidad), 0) FROM ventas
        WHERE negocioId = :negocioId
        AND fechaVenta = :fechaVenta
        AND estado = :estado
    """)
    fun observarPiezasPorNegocioFechaYEstado(
        negocioId: String,
        fechaVenta: String,
        estado: String
    ): Flow<Int>

    @Query("""
        UPDATE ventas
        SET estado = :nuevoEstado,
            actualizadoEn = :actualizadoEn
        WHERE id = :ventaId
    """)
    suspend fun actualizarEstado(
        ventaId: String,
        nuevoEstado: String,
        actualizadoEn: Long
    )

    @Query("""
        UPDATE ventas
        SET corteId = :corteId,
            estado = :nuevoEstado,
            syncEstado = :syncEstado,
            actualizadoEn = :actualizadoEn
        WHERE id IN (:ventaIds)
    """)
    suspend fun cerrarVentasConCorte(
        ventaIds: List<String>,
        corteId: String,
        nuevoEstado: String,
        syncEstado: String,
        actualizadoEn: Long
    )

    @Query("""
        SELECT COUNT(*) FROM ventas
        WHERE productoId = :productoId
    """)
    suspend fun contarVentasPorProducto(productoId: String): Int
}
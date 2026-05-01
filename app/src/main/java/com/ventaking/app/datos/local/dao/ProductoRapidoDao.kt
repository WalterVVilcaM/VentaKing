package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.ProductoRapidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoRapidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoRapidoEntity)

    @Update
    suspend fun actualizar(producto: ProductoRapidoEntity)

    @Query("""
        SELECT * FROM productos_rapidos
        WHERE negocioId = :negocioId
        ORDER BY estaActivo DESC, nombre ASC
    """)
    fun observarPorNegocio(negocioId: String): Flow<List<ProductoRapidoEntity>>

    @Query("""
        SELECT * FROM productos_rapidos
        WHERE negocioId = :negocioId
        AND estaActivo = 1
        ORDER BY nombre ASC
    """)
    fun observarActivosPorNegocio(negocioId: String): Flow<List<ProductoRapidoEntity>>

    @Query("SELECT * FROM productos_rapidos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): ProductoRapidoEntity?

    @Query("""
        UPDATE productos_rapidos
        SET estaActivo = 0, actualizadoEn = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun desactivar(id: String, actualizadoEn: Long)

    @Query("""
        SELECT COUNT(*) FROM productos_rapidos
        WHERE negocioId = :negocioId
    """)
    suspend fun contarPorNegocio(negocioId: String): Int
}
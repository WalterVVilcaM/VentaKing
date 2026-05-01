package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.ProductoRapido
import kotlinx.coroutines.flow.Flow

interface ProductoRapidoRepository {

    fun observarProductosPorNegocio(
        negocioId: String
    ): Flow<List<ProductoRapido>>

    fun observarProductosActivosPorNegocio(
        negocioId: String
    ): Flow<List<ProductoRapido>>

    suspend fun obtenerProductoPorId(
        id: String
    ): ProductoRapido?

    suspend fun crearProducto(
        producto: ProductoRapido
    )

    suspend fun editarProducto(
        producto: ProductoRapido
    )

    suspend fun desactivarProducto(
        id: String,
        actualizadoEn: Long
    )

    suspend fun reactivarProducto(
        id: String,
        actualizadoEn: Long
    )
}
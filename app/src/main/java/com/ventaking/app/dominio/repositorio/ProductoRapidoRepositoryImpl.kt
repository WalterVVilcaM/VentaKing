package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.local.dao.ProductoRapidoDao
import com.ventaking.app.datos.local.entidades.ProductoRapidoEntity
import com.ventaking.app.dominio.modelo.ProductoRapido
import com.ventaking.app.dominio.repositorio.ProductoRapidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductoRapidoRepositoryImpl(
    private val productoRapidoDao: ProductoRapidoDao
) : ProductoRapidoRepository {

    override fun observarProductosPorNegocio(
        negocioId: String
    ): Flow<List<ProductoRapido>> {
        return productoRapidoDao
            .observarPorNegocio(negocioId)
            .map { productos ->
                productos.map { it.toDomain() }
            }
    }

    override fun observarProductosActivosPorNegocio(
        negocioId: String
    ): Flow<List<ProductoRapido>> {
        return productoRapidoDao
            .observarActivosPorNegocio(negocioId)
            .map { productos ->
                productos.map { it.toDomain() }
            }
    }

    override suspend fun obtenerProductoPorId(
        id: String
    ): ProductoRapido? {
        return productoRapidoDao
            .obtenerPorId(id)
            ?.toDomain()
    }

    override suspend fun crearProducto(
        producto: ProductoRapido
    ) {
        productoRapidoDao.insertar(producto.toEntity())
    }

    override suspend fun editarProducto(
        producto: ProductoRapido
    ) {
        productoRapidoDao.actualizar(producto.toEntity())
    }

    override suspend fun desactivarProducto(
        id: String,
        actualizadoEn: Long
    ) {
        productoRapidoDao.desactivar(
            id = id,
            actualizadoEn = actualizadoEn
        )
    }

    override suspend fun reactivarProducto(
        id: String,
        actualizadoEn: Long
    ) {
        productoRapidoDao.reactivar(
            id = id,
            actualizadoEn = actualizadoEn
        )
    }
}

private fun ProductoRapidoEntity.toDomain(): ProductoRapido {
    return ProductoRapido(
        id = id,
        negocioId = negocioId,
        nombre = nombre,
        precioCentavos = precioCentavos,
        categoria = categoria,
        esPromocion = esPromocion,
        estaActivo = estaActivo,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )
}

private fun ProductoRapido.toEntity(): ProductoRapidoEntity {
    return ProductoRapidoEntity(
        id = id,
        negocioId = negocioId,
        nombre = nombre,
        precioCentavos = precioCentavos,
        categoria = categoria,
        esPromocion = esPromocion,
        estaActivo = estaActivo,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )
}
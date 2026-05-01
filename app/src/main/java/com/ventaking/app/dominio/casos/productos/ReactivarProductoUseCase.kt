package com.ventaking.app.dominio.casos.productos

import com.ventaking.app.dominio.repositorio.ProductoRapidoRepository

class ReactivarProductoUseCase(
    private val productoRapidoRepository: ProductoRapidoRepository
) {

    suspend operator fun invoke(
        productoId: String
    ) {
        require(productoId.isNotBlank()) {
            "No se recibió el producto que se desea reactivar."
        }

        productoRapidoRepository.reactivarProducto(
            id = productoId,
            actualizadoEn = System.currentTimeMillis()
        )
    }
}
package com.ventaking.app.dominio.casos.productos

import com.ventaking.app.dominio.repositorio.ProductoRapidoRepository

class EditarProductoUseCase(
    private val productoRapidoRepository: ProductoRapidoRepository
) {

    suspend operator fun invoke(
        productoId: String,
        nombre: String,
        precioCentavos: Long,
        categoria: String?,
        esPromocion: Boolean
    ) {
        val productoActual = productoRapidoRepository.obtenerProductoPorId(productoId)
            ?: throw IllegalArgumentException("No se encontró el producto seleccionado.")

        val nombreLimpio = nombre.trim()
        val categoriaLimpia = categoria?.trim()?.takeIf { it.isNotBlank() }

        require(nombreLimpio.isNotBlank()) {
            "El nombre del producto no puede estar vacío."
        }

        require(precioCentavos > 0) {
            "El precio debe ser mayor a cero."
        }

        val productoEditado = productoActual.copy(
            nombre = nombreLimpio,
            precioCentavos = precioCentavos,
            categoria = categoriaLimpia,
            esPromocion = esPromocion,
            actualizadoEn = System.currentTimeMillis()
        )

        productoRapidoRepository.editarProducto(productoEditado)
    }
}
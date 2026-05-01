package com.ventaking.app.dominio.casos.productos

import com.ventaking.app.dominio.modelo.ProductoRapido
import com.ventaking.app.dominio.repositorio.ProductoRapidoRepository
import java.util.UUID

class CrearProductoUseCase(
    private val productoRapidoRepository: ProductoRapidoRepository
) {

    suspend operator fun invoke(
        negocioId: String,
        nombre: String,
        precioCentavos: Long,
        categoria: String?,
        esPromocion: Boolean
    ) {
        val nombreLimpio = nombre.trim()
        val categoriaLimpia = categoria?.trim()?.takeIf { it.isNotBlank() }

        require(negocioId.isNotBlank()) {
            "Debe seleccionarse un negocio."
        }

        require(nombreLimpio.isNotBlank()) {
            "El nombre del producto no puede estar vacío."
        }

        require(precioCentavos > 0) {
            "El precio debe ser mayor a cero."
        }

        val ahora = System.currentTimeMillis()

        val producto = ProductoRapido(
            id = UUID.randomUUID().toString(),
            negocioId = negocioId,
            nombre = nombreLimpio,
            precioCentavos = precioCentavos,
            categoria = categoriaLimpia,
            esPromocion = esPromocion,
            estaActivo = true,
            creadoEn = ahora,
            actualizadoEn = ahora
        )

        productoRapidoRepository.crearProducto(producto)
    }
}
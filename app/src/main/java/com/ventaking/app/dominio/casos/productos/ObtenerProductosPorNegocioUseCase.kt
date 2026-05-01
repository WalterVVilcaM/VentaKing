package com.ventaking.app.dominio.casos.productos

import com.ventaking.app.dominio.modelo.ProductoRapido
import com.ventaking.app.dominio.repositorio.ProductoRapidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObtenerProductosPorNegocioUseCase(
    private val productoRapidoRepository: ProductoRapidoRepository
) {

    operator fun invoke(
        negocioId: String,
        soloActivos: Boolean = false
    ): Flow<List<ProductoRapido>> {
        if (negocioId.isBlank()) {
            return flowOf(emptyList())
        }

        return if (soloActivos) {
            productoRapidoRepository.observarProductosActivosPorNegocio(negocioId)
        } else {
            productoRapidoRepository.observarProductosPorNegocio(negocioId)
        }
    }
}
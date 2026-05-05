package com.ventaking.app.dominio.casos.ventas

import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.VentaRepository
import kotlinx.coroutines.flow.Flow

class ObtenerVentasDelDiaUseCase(
    private val ventaRepository: VentaRepository
) {

    operator fun invoke(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<Venta>> {
        return ventaRepository.observarVentasActivasPorNegocioYFecha(
            negocioId = negocioId,
            fechaVenta = fechaVenta
        )
    }
}
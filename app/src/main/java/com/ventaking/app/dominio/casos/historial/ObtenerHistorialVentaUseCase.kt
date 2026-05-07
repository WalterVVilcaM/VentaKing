package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow

class ObtenerHistorialVentaUseCase(
    private val historialRepository: HistorialRepository
) {

    operator fun invoke(
        ventaId: String
    ): Flow<List<HistorialVenta>> {
        return historialRepository.observarHistorialDeVenta(
            ventaId = ventaId
        )
    }
}
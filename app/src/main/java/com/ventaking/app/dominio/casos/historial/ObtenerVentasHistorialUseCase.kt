package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow

class ObtenerVentasHistorialUseCase(
    private val historialRepository: HistorialRepository
) {

    operator fun invoke(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String?,
        estado: String?
    ): Flow<List<Venta>> {
        return historialRepository.observarVentasHistorial(
            negocioId = negocioId,
            fechaVenta = fechaVenta,
            dispositivoId = dispositivoId,
            estado = estado
        )
    }
}
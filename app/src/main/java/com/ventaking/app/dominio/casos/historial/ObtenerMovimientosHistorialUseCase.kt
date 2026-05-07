package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow

class ObtenerMovimientosHistorialUseCase(
    private val historialRepository: HistorialRepository
) {

    operator fun invoke(
        negocioId: String,
        dispositivoId: String?,
        desde: Long,
        hasta: Long
    ): Flow<List<HistorialVenta>> {
        return historialRepository.observarMovimientosHistorial(
            negocioId = negocioId,
            dispositivoId = dispositivoId,
            desde = desde,
            hasta = hasta
        )
    }
}
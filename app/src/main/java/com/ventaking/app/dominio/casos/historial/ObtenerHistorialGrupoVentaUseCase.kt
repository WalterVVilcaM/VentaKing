package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow

class ObtenerHistorialGrupoVentaUseCase(
    private val historialRepository: HistorialRepository
) {

    operator fun invoke(
        grupoVentaId: String
    ): Flow<List<HistorialVenta>> {
        return historialRepository.observarHistorialDeGrupoVenta(
            grupoVentaId = grupoVentaId
        )
    }
}
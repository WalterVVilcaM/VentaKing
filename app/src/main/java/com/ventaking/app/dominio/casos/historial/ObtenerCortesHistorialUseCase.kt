package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow

class ObtenerCortesHistorialUseCase(
    private val historialRepository: HistorialRepository
) {

    operator fun invoke(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String?
    ): Flow<List<CorteDiario>> {
        return historialRepository.observarCortesHistorial(
            negocioId = negocioId,
            fechaCorte = fechaCorte,
            dispositivoId = dispositivoId
        )
    }
}
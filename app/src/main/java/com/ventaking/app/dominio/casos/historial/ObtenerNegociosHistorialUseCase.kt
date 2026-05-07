package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.Negocio
import com.ventaking.app.dominio.repositorio.NegocioRepository
import kotlinx.coroutines.flow.Flow

class ObtenerNegociosHistorialUseCase(
    private val negocioRepository: NegocioRepository
) {

    operator fun invoke(): Flow<List<Negocio>> {
        return negocioRepository.observarNegociosActivos()
    }
}
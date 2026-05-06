package com.ventaking.app.dominio.casos.corte

import com.ventaking.app.dominio.modelo.ResumenCorte
import com.ventaking.app.dominio.repositorio.CorteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtenerResumenCorteUseCase(
    private val corteRepository: CorteRepository
) {

    operator fun invoke(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): Flow<ResumenCorte> {
        return corteRepository.observarVentasAbiertasParaCorte(
            negocioId = negocioId,
            fechaCorte = fechaCorte,
            dispositivoId = dispositivoId
        ).map { ventas ->
            val corteExistente = corteRepository.obtenerCorteExistente(
                negocioId = negocioId,
                fechaCorte = fechaCorte,
                dispositivoId = dispositivoId
            )

            ResumenCorte(
                negocioId = negocioId,
                fechaCorte = fechaCorte,
                dispositivoId = dispositivoId,
                ventas = ventas,
                corteExistente = corteExistente
            )
        }
    }
}
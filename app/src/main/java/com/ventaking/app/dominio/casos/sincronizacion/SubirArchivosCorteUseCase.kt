package com.ventaking.app.dominio.casos.sincronizacion

import com.ventaking.app.dominio.repositorio.ResultadoSubidaCorte
import com.ventaking.app.dominio.repositorio.SincronizacionRepository

class SubirArchivosCorteUseCase(
    private val sincronizacionRepository: SincronizacionRepository
) {

    suspend operator fun invoke(corteId: String): ResultadoSubidaCorte {
        return sincronizacionRepository.subirArchivosCorte(corteId)
    }
}
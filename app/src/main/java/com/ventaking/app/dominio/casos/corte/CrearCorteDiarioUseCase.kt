package com.ventaking.app.dominio.casos.corte

import com.ventaking.app.dominio.repositorio.CorteRepository
import com.ventaking.app.dominio.repositorio.ResultadoCrearCorteDiario

class CrearCorteDiarioUseCase(
    private val corteRepository: CorteRepository
) {

    suspend operator fun invoke(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): ResultadoCrearCorteDiario {
        if (negocioId.isBlank()) {
            return ResultadoCrearCorteDiario.Error("Selecciona un negocio válido.")
        }

        if (fechaCorte.isBlank()) {
            return ResultadoCrearCorteDiario.Error("La fecha del corte no es válida.")
        }

        if (dispositivoId.isBlank()) {
            return ResultadoCrearCorteDiario.Error("No se encontró el dispositivo local.")
        }

        return corteRepository.crearCorteDiario(
            negocioId = negocioId,
            fechaCorte = fechaCorte,
            dispositivoId = dispositivoId
        )
    }
}
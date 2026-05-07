package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.modelo.Dispositivo
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import kotlinx.coroutines.flow.Flow

class ObtenerDispositivoActualHistorialUseCase(
    private val dispositivoRepository: DispositivoRepository
) {

    operator fun invoke(): Flow<Dispositivo?> {
        return dispositivoRepository.observarDispositivoActual()
    }
}
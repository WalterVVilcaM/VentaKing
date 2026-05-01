package com.ventaking.app.dominio.casos

import com.ventaking.app.dominio.repositorio.ConfiguracionAppRepository
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import com.ventaking.app.dominio.repositorio.NegocioRepository

class InicializarAppUseCase(
    private val negocioRepository: NegocioRepository,
    private val dispositivoRepository: DispositivoRepository,
    private val configuracionAppRepository: ConfiguracionAppRepository
) {

    suspend operator fun invoke() {
        negocioRepository.insertarNegociosBaseSiNoExisten()
        dispositivoRepository.crearDispositivoSiNoExiste()
        configuracionAppRepository.crearConfiguracionInicialSiNoExiste()
    }
}
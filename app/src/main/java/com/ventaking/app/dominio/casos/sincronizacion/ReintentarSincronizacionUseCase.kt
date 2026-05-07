package com.ventaking.app.dominio.casos.sincronizacion

import com.ventaking.app.dominio.repositorio.ResultadoSubidaCorte
import com.ventaking.app.dominio.repositorio.SincronizacionRepository

sealed class ResultadoReintentoSincronizacion {
    data class Exito(
        val archivosSubidos: Int,
        val archivosOmitidos: Int
    ) : ResultadoReintentoSincronizacion()

    data object SinInternet : ResultadoReintentoSincronizacion()

    data class Error(
        val mensaje: String
    ) : ResultadoReintentoSincronizacion()
}

class ReintentarSincronizacionUseCase(
    private val sincronizacionRepository: SincronizacionRepository,
    private val verificarConexionUseCase: VerificarConexionUseCase
) {

    suspend operator fun invoke(corteId: String): ResultadoReintentoSincronizacion {
        if (!verificarConexionUseCase()) {
            return ResultadoReintentoSincronizacion.SinInternet
        }

        return when (val resultado = sincronizacionRepository.subirArchivosCorte(corteId)) {
            is ResultadoSubidaCorte.Exito -> {
                ResultadoReintentoSincronizacion.Exito(
                    archivosSubidos = resultado.archivosSubidos,
                    archivosOmitidos = resultado.archivosOmitidos
                )
            }

            is ResultadoSubidaCorte.Error -> {
                ResultadoReintentoSincronizacion.Error(resultado.mensaje)
            }
        }
    }
}
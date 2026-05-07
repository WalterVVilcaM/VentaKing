package com.ventaking.app.dominio.casos.sincronizacion

import com.ventaking.app.nucleo.red.MonitorConexion

class VerificarConexionUseCase(
    private val monitorConexion: MonitorConexion
) {

    operator fun invoke(): Boolean {
        return monitorConexion.hayInternet()
    }
}
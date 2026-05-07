package com.ventaking.app.datos.drive

import com.google.api.services.drive.Drive

class DriveServiceProvider {

    @Volatile
    private var driveService: Drive? = null

    fun configurar(drive: Drive) {
        driveService = drive
    }

    fun estaConectado(): Boolean {
        return driveService != null
    }

    fun obtenerDataSource(): DriveDataSource? {
        val service = driveService ?: return null
        return DriveDataSource(service)
    }

    fun cerrarConexion() {
        driveService = null
    }
}
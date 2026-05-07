package com.ventaking.app.datos.drive

sealed class ResultadoDrive {
    data class Exito(
        val driveFileId: String
    ) : ResultadoDrive()

    data class Error(
        val mensaje: String,
        val excepcion: Throwable? = null
    ) : ResultadoDrive()
}
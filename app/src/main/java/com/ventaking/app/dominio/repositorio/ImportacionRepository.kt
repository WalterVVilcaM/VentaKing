package com.ventaking.app.dominio.repositorio

interface ImportacionRepository {

    suspend fun importarVentasJson(
        contenidoJson: String
    ): ResultadoImportacion
}

sealed class ResultadoImportacion {
    data class Exito(
        val mensaje: String,
        val ventasImportadas: Int,
        val corteImportadoId: String
    ) : ResultadoImportacion()

    data class Error(
        val mensaje: String
    ) : ResultadoImportacion()
}
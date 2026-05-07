package com.ventaking.app.dominio.repositorio

sealed class ResultadoSubidaCorte {
    data class Exito(
        val archivosSubidos: Int,
        val archivosOmitidos: Int
    ) : ResultadoSubidaCorte()

    data class Error(
        val mensaje: String
    ) : ResultadoSubidaCorte()
}

interface SincronizacionRepository {

    suspend fun subirArchivosCorte(corteId: String): ResultadoSubidaCorte
}
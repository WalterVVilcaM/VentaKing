package com.ventaking.app.dominio.repositorio

import kotlinx.coroutines.flow.Flow

data class CortePendienteSync(
    val id: String,
    val negocioId: String,
    val fechaCorte: String,
    val dispositivoId: String,
    val totalCentavos: Long,
    val totalVentas: Int,
    val totalPiezas: Int,
    val creadoEn: Long,
    val syncEstado: String,
    val mensajeError: String?
)

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

    fun observarCortesPendientes(): Flow<List<CortePendienteSync>>

    suspend fun obtenerCortesPendientes(): List<CortePendienteSync>
}
package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.ArchivoExportado

interface ExportacionRepository {

    suspend fun exportarVentasJson(corteId: String): ResultadoExportacion

    suspend fun exportarCorteJson(corteId: String): ResultadoExportacion

    suspend fun exportarCorteExcel(corteId: String): ResultadoExportacion
}

sealed class ResultadoExportacion {
    data class Exito(val archivo: ArchivoExportado) : ResultadoExportacion()
    data class Error(val mensaje: String) : ResultadoExportacion()
}
package com.ventaking.app.dominio.casos.exportacion

import com.ventaking.app.dominio.repositorio.ExportacionRepository
import com.ventaking.app.dominio.repositorio.ResultadoExportacion

class ExportarCorteExcelUseCase(
    private val exportacionRepository: ExportacionRepository
) {
    suspend operator fun invoke(corteId: String): ResultadoExportacion {
        return exportacionRepository.exportarCorteExcel(corteId)
    }
}
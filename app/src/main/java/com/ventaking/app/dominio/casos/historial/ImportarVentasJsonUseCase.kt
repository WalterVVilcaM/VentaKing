package com.ventaking.app.dominio.casos.historial

import com.ventaking.app.dominio.repositorio.ImportacionRepository
import com.ventaking.app.dominio.repositorio.ResultadoImportacion

class ImportarVentasJsonUseCase(
    private val importacionRepository: ImportacionRepository
) {

    suspend operator fun invoke(
        contenidoJson: String
    ): ResultadoImportacion {
        return importacionRepository.importarVentasJson(
            contenidoJson = contenidoJson
        )
    }
}
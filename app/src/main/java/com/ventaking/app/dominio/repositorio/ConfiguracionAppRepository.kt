package com.ventaking.app.dominio.repositorio

import com.ventaking.app.datos.local.entidades.ConfiguracionAppEntity

interface ConfiguracionAppRepository {

    suspend fun crearConfiguracionInicialSiNoExiste()

    suspend fun obtenerConfiguracionActual(): ConfiguracionAppEntity?

    suspend fun actualizarTema(tema: String)

    suspend fun actualizarCarpetaDrive(
        carpetaDriveId: String?,
        carpetaDriveNombre: String?
    )
}
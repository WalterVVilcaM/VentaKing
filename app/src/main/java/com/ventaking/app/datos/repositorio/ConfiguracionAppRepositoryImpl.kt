package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.local.dao.ConfiguracionAppDao
import com.ventaking.app.datos.local.entidades.ConfiguracionAppEntity
import com.ventaking.app.dominio.repositorio.ConfiguracionAppRepository
import com.ventaking.app.nucleo.constantes.TemaApp
import java.util.UUID

class ConfiguracionAppRepositoryImpl(
    private val configuracionAppDao: ConfiguracionAppDao
) : ConfiguracionAppRepository {

    override suspend fun crearConfiguracionInicialSiNoExiste() {
        val configuracionActual = configuracionAppDao.obtenerActual()

        if (configuracionActual != null) return

        val fechaActual = System.currentTimeMillis()

        val configuracionInicial = ConfiguracionAppEntity(
            id = UUID.randomUUID().toString(),
            tema = TemaApp.SISTEMA,
            carpetaDriveId = null,
            carpetaDriveNombre = null,
            creadoEn = fechaActual,
            actualizadoEn = fechaActual
        )

        configuracionAppDao.insertar(configuracionInicial)
    }

    override suspend fun obtenerConfiguracionActual(): ConfiguracionAppEntity? {
        return configuracionAppDao.obtenerActual()
    }

    override suspend fun actualizarTema(tema: String) {
        val configuracionActual = configuracionAppDao.obtenerActual() ?: return

        configuracionAppDao.actualizarTema(
            id = configuracionActual.id,
            tema = tema,
            actualizadoEn = System.currentTimeMillis()
        )
    }

    override suspend fun actualizarCarpetaDrive(
        carpetaDriveId: String?,
        carpetaDriveNombre: String?
    ) {
        val configuracionActual = configuracionAppDao.obtenerActual() ?: return

        configuracionAppDao.actualizarCarpetaDrive(
            id = configuracionActual.id,
            carpetaDriveId = carpetaDriveId,
            carpetaDriveNombre = carpetaDriveNombre,
            actualizadoEn = System.currentTimeMillis()
        )
    }
}
package com.ventaking.app.datos.repositorio

import android.os.Build
import com.ventaking.app.datos.local.dao.DispositivoDao
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import java.util.UUID

class DispositivoRepositoryImpl(
    private val dispositivoDao: DispositivoDao
) : DispositivoRepository {

    override suspend fun crearDispositivoSiNoExiste() {
        val dispositivoActual = dispositivoDao.obtenerActual()

        if (dispositivoActual != null) return

        val fechaActual = System.currentTimeMillis()

        val nombreSugerido = if (Build.MODEL.isNullOrBlank()) {
            "Dispositivo local"
        } else {
            "Dispositivo ${Build.MODEL}"
        }

        val nuevoDispositivo = DispositivoEntity(
            id = UUID.randomUUID().toString(),
            nombreDispositivo = nombreSugerido,
            creadoEn = fechaActual,
            actualizadoEn = fechaActual,
            ultimoRespaldoEn = null
        )

        dispositivoDao.insertar(nuevoDispositivo)
    }

    override suspend fun obtenerDispositivoActual(): DispositivoEntity? {
        return dispositivoDao.obtenerActual()
    }

    override suspend fun obtenerIdDispositivoActual(): String? {
        return dispositivoDao.obtenerActual()?.id
    }

    override suspend fun actualizarNombreDispositivo(nombreDispositivo: String?) {
        val dispositivoActual = dispositivoDao.obtenerActual() ?: return

        dispositivoDao.actualizarNombre(
            id = dispositivoActual.id,
            nombreDispositivo = nombreDispositivo?.trim(),
            actualizadoEn = System.currentTimeMillis()
        )
    }
}
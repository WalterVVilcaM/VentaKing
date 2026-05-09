package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.drive.DriveServiceProvider
import com.ventaking.app.datos.drive.ResultadoDrive
import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.DispositivoDao
import com.ventaking.app.datos.local.dao.RegistroArchivoSyncDao
import com.ventaking.app.datos.local.entidades.RegistroArchivoSyncEntity
import com.ventaking.app.dominio.repositorio.CortePendienteSync
import com.ventaking.app.dominio.repositorio.ResultadoSubidaCorte
import com.ventaking.app.dominio.repositorio.SincronizacionRepository
import com.ventaking.app.nucleo.constantes.GoogleDriveConfig
import com.ventaking.app.nucleo.constantes.SyncEstado
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SincronizacionRepositoryImpl(
    private val registroArchivoSyncDao: RegistroArchivoSyncDao,
    private val corteDiarioDao: CorteDiarioDao,
    private val dispositivoDao: DispositivoDao,
    private val driveServiceProvider: DriveServiceProvider
) : SincronizacionRepository {

    override suspend fun subirArchivosCorte(corteId: String): ResultadoSubidaCorte {
        val ahora = System.currentTimeMillis()

        val driveDataSource = driveServiceProvider.obtenerDataSource()

        if (driveDataSource == null) {
            val mensaje = "Google Drive no está conectado. El corte queda pendiente de respaldo."

            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.PENDING_SYNC,
                sincronizadoEn = null,
                mensajeError = mensaje
            )

            return ResultadoSubidaCorte.Error(mensaje)
        }

        val registros = registroArchivoSyncDao.obtenerPorCorte(corteId)

        if (registros.isEmpty()) {
            val mensaje = "No hay archivos locales registrados para respaldar este corte."

            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.SYNC_ERROR,
                sincronizadoEn = null,
                mensajeError = mensaje
            )

            return ResultadoSubidaCorte.Error(mensaje)
        }

        val registrosParaSubir = obtenerRegistrosParaSubir(registros)

        if (registrosParaSubir.isEmpty()) {
            val mensaje = "El corte no tiene archivos válidos para subir a Drive."

            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.SYNC_ERROR,
                sincronizadoEn = null,
                mensajeError = mensaje
            )

            return ResultadoSubidaCorte.Error(mensaje)
        }

        val tieneZipProtegido = registrosParaSubir.any {
            it.tipoArchivo == TipoArchivo.CORTE_ZIP_PROTEGIDO
        }

        if (!tieneZipProtegido && registrosParaSubir.size < TOTAL_ARCHIVOS_OBLIGATORIOS_LEGACY) {
            val mensaje = "El corte no tiene ZIP protegido ni los tres archivos obligatorios para subir a Drive."

            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.SYNC_ERROR,
                sincronizadoEn = null,
                mensajeError = mensaje
            )

            return ResultadoSubidaCorte.Error(mensaje)
        }

        val carpetaRaizId = when (
            val resultado = driveDataSource.obtenerOCrearCarpeta(
                nombreCarpeta = GoogleDriveConfig.CARPETA_RAIZ_DRIVE
            )
        ) {
            is ResultadoDrive.Exito -> resultado.driveFileId
            is ResultadoDrive.Error -> {
                marcarCorteConError(corteId, resultado.mensaje)
                return ResultadoSubidaCorte.Error(resultado.mensaje)
            }
        }

        val carpetaNegocioNombre = obtenerCarpetaNegocio(registrosParaSubir.first())

        val carpetaNegocioId = when (
            val resultado = driveDataSource.obtenerOCrearCarpeta(
                nombreCarpeta = carpetaNegocioNombre,
                parentId = carpetaRaizId
            )
        ) {
            is ResultadoDrive.Exito -> resultado.driveFileId
            is ResultadoDrive.Error -> {
                marcarCorteConError(corteId, resultado.mensaje)
                return ResultadoSubidaCorte.Error(resultado.mensaje)
            }
        }

        val fechaCorte = registrosParaSubir.first().fechaCorte

        val carpetaFechaId = when (
            val resultado = driveDataSource.obtenerOCrearCarpeta(
                nombreCarpeta = fechaCorte,
                parentId = carpetaNegocioId
            )
        ) {
            is ResultadoDrive.Exito -> resultado.driveFileId
            is ResultadoDrive.Error -> {
                marcarCorteConError(corteId, resultado.mensaje)
                return ResultadoSubidaCorte.Error(resultado.mensaje)
            }
        }

        var archivosSubidos = 0
        var archivosOmitidos = 0
        val errores = mutableListOf<String>()

        registrosParaSubir.forEach { registro ->
            if (registro.syncEstado == SyncEstado.SYNCED && registro.driveFileId != null) {
                archivosOmitidos++
                return@forEach
            }

            val archivoLocal = File(registro.rutaLocal)

            if (!archivoLocal.exists()) {
                val mensaje = "Archivo local no encontrado: ${registro.nombreArchivo}"
                errores.add(mensaje)

                registroArchivoSyncDao.actualizarResultadoSync(
                    id = registro.id,
                    driveFileId = registro.driveFileId,
                    syncEstado = SyncEstado.SYNC_ERROR,
                    sincronizadoEn = null,
                    actualizadoEn = ahora,
                    mensajeError = mensaje
                )

                return@forEach
            }

            when (
                val resultado = driveDataSource.subirArchivoSiNoExiste(
                    carpetaDriveId = carpetaFechaId,
                    nombreArchivo = registro.nombreArchivo,
                    rutaLocal = registro.rutaLocal,
                    hashArchivo = registro.hashArchivo,
                    corteId = registro.corteId,
                    tipoArchivo = registro.tipoArchivo
                )
            ) {
                is ResultadoDrive.Exito -> {
                    registroArchivoSyncDao.actualizarResultadoSync(
                        id = registro.id,
                        driveFileId = resultado.driveFileId,
                        syncEstado = SyncEstado.SYNCED,
                        sincronizadoEn = ahora,
                        actualizadoEn = ahora,
                        mensajeError = null
                    )

                    archivosSubidos++
                }

                is ResultadoDrive.Error -> {
                    errores.add(resultado.mensaje)

                    registroArchivoSyncDao.actualizarResultadoSync(
                        id = registro.id,
                        driveFileId = registro.driveFileId,
                        syncEstado = SyncEstado.SYNC_ERROR,
                        sincronizadoEn = null,
                        actualizadoEn = ahora,
                        mensajeError = resultado.mensaje
                    )
                }
            }
        }

        return if (errores.isEmpty()) {
            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.SYNCED,
                sincronizadoEn = ahora,
                mensajeError = null
            )

            dispositivoDao.obtenerActual()?.let { dispositivo ->
                dispositivoDao.actualizarUltimoRespaldo(
                    id = dispositivo.id,
                    ultimoRespaldoEn = ahora,
                    actualizadoEn = ahora
                )
            }

            ResultadoSubidaCorte.Exito(
                archivosSubidos = archivosSubidos,
                archivosOmitidos = archivosOmitidos
            )
        } else {
            val mensajeFinal = errores.joinToString(separator = " | ")

            corteDiarioDao.actualizarSyncEstado(
                corteId = corteId,
                syncEstado = SyncEstado.SYNC_ERROR,
                sincronizadoEn = null,
                mensajeError = mensajeFinal
            )

            ResultadoSubidaCorte.Error(mensajeFinal)
        }
    }

    override fun observarCortesPendientes(): Flow<List<CortePendienteSync>> {
        return corteDiarioDao.observarPorEstadosSync(
            estados = listOf(
                SyncEstado.PENDING_SYNC,
                SyncEstado.SYNC_ERROR
            )
        ).map { cortes ->
            cortes.map { corte ->
                CortePendienteSync(
                    id = corte.id,
                    negocioId = corte.negocioId,
                    fechaCorte = corte.fechaCorte,
                    dispositivoId = corte.dispositivoId,
                    totalCentavos = corte.totalCentavos,
                    totalVentas = corte.totalVentas,
                    totalPiezas = corte.totalPiezas,
                    creadoEn = corte.creadoEn,
                    syncEstado = corte.syncEstado,
                    mensajeError = corte.mensajeError
                )
            }
        }
    }

    override suspend fun obtenerCortesPendientes(): List<CortePendienteSync> {
        return corteDiarioDao.obtenerListaPorEstadosSync(
            estados = listOf(
                SyncEstado.PENDING_SYNC,
                SyncEstado.SYNC_ERROR
            )
        ).map { corte ->
            CortePendienteSync(
                id = corte.id,
                negocioId = corte.negocioId,
                fechaCorte = corte.fechaCorte,
                dispositivoId = corte.dispositivoId,
                totalCentavos = corte.totalCentavos,
                totalVentas = corte.totalVentas,
                totalPiezas = corte.totalPiezas,
                creadoEn = corte.creadoEn,
                syncEstado = corte.syncEstado,
                mensajeError = corte.mensajeError
            )
        }
    }

    private fun obtenerRegistrosParaSubir(
        registros: List<RegistroArchivoSyncEntity>
    ): List<RegistroArchivoSyncEntity> {
        val zipProtegido = registros.firstOrNull {
            it.tipoArchivo == TipoArchivo.CORTE_ZIP_PROTEGIDO
        }

        if (zipProtegido != null) {
            return listOf(zipProtegido)
        }

        return registros.filter { registro ->
            registro.tipoArchivo == TipoArchivo.VENTAS_JSON ||
                    registro.tipoArchivo == TipoArchivo.CORTE_JSON ||
                    registro.tipoArchivo == TipoArchivo.CORTE_EXCEL
        }
    }

    private suspend fun marcarCorteConError(
        corteId: String,
        mensaje: String
    ) {
        corteDiarioDao.actualizarSyncEstado(
            corteId = corteId,
            syncEstado = SyncEstado.SYNC_ERROR,
            sincronizadoEn = null,
            mensajeError = mensaje
        )
    }

    private fun obtenerCarpetaNegocio(
        registro: RegistroArchivoSyncEntity
    ): String {
        val archivo = File(registro.rutaLocal)

        return archivo.parentFile
            ?.parentFile
            ?.name
            ?.takeIf { it.isNotBlank() }
            ?: registro.negocioId
    }

    private object TipoArchivo {
        const val VENTAS_JSON = "VENTAS_JSON"
        const val CORTE_JSON = "CORTE_JSON"
        const val CORTE_EXCEL = "CORTE_EXCEL"
        const val CORTE_ZIP_PROTEGIDO = "CORTE_ZIP_PROTEGIDO"
    }

    private companion object {
        const val TOTAL_ARCHIVOS_OBLIGATORIOS_LEGACY = 3
    }
}
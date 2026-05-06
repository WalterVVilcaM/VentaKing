package com.ventaking.app.datos.repositorio

import android.content.Context
import com.ventaking.app.datos.excel.GeneradorExcel
import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.DispositivoDao
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.datos.local.dao.RegistroArchivoSyncDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.RegistroArchivoSyncEntity
import com.ventaking.app.datos.respaldo.GeneradorJson
import com.ventaking.app.dominio.modelo.ArchivoExportado
import com.ventaking.app.dominio.repositorio.ExportacionRepository
import com.ventaking.app.dominio.repositorio.ResultadoExportacion
import com.ventaking.app.nucleo.constantes.SyncEstado
import com.ventaking.app.nucleo.constantes.VentaEstado
import java.io.File
import java.security.MessageDigest
import java.text.Normalizer
import java.util.UUID

class ExportacionRepositoryImpl(
    private val context: Context,
    private val corteDiarioDao: CorteDiarioDao,
    private val ventaDao: VentaDao,
    private val negocioDao: NegocioDao,
    private val dispositivoDao: DispositivoDao,
    private val registroArchivoSyncDao: RegistroArchivoSyncDao,
    private val generadorJson: GeneradorJson,
    private val generadorExcel: GeneradorExcel
) : ExportacionRepository {

    override suspend fun exportarVentasJson(corteId: String): ResultadoExportacion {
        return try {
            val datos = obtenerDatosBase(corteId)
                ?: return ResultadoExportacion.Error("No se encontró información suficiente para exportar ventas.")

            val nombreArchivo = construirNombreArchivo(
                negocioNombre = datos.negocio.nombre,
                fechaCorte = datos.corte.fechaCorte,
                dispositivoId = datos.dispositivo.id,
                corteId = datos.corte.id,
                tipo = "ventas",
                extension = "json"
            )

            val contenidoInicial = generadorJson.construirVentasJson(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = null
            )

            val hash = calcularHash(contenidoInicial.toByteArray(Charsets.UTF_8))

            val contenidoFinal = generadorJson.construirVentasJson(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = hash
            )

            guardarArchivoYRegistro(
                corteId = corteId,
                negocioId = datos.corte.negocioId,
                dispositivoId = datos.corte.dispositivoId,
                fechaCorte = datos.corte.fechaCorte,
                tipoArchivo = TipoArchivo.VENTAS_JSON,
                nombreArchivo = nombreArchivo,
                bytes = contenidoFinal.toByteArray(Charsets.UTF_8),
                hashArchivo = calcularHash(contenidoFinal.toByteArray(Charsets.UTF_8)),
                negocioNombre = datos.negocio.nombre
            )
        } catch (e: Exception) {
            ResultadoExportacion.Error(
                mensaje = e.message ?: "No se pudo exportar ventas JSON."
            )
        }
    }

    override suspend fun exportarCorteJson(corteId: String): ResultadoExportacion {
        return try {
            val datos = obtenerDatosBase(corteId)
                ?: return ResultadoExportacion.Error("No se encontró información suficiente para exportar corte.")

            val nombreArchivo = construirNombreArchivo(
                negocioNombre = datos.negocio.nombre,
                fechaCorte = datos.corte.fechaCorte,
                dispositivoId = datos.dispositivo.id,
                corteId = datos.corte.id,
                tipo = "corte",
                extension = "json"
            )

            val contenidoInicial = generadorJson.construirCorteJson(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = null
            )

            val hash = calcularHash(contenidoInicial.toByteArray(Charsets.UTF_8))

            val contenidoFinal = generadorJson.construirCorteJson(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = hash
            )

            guardarArchivoYRegistro(
                corteId = corteId,
                negocioId = datos.corte.negocioId,
                dispositivoId = datos.corte.dispositivoId,
                fechaCorte = datos.corte.fechaCorte,
                tipoArchivo = TipoArchivo.CORTE_JSON,
                nombreArchivo = nombreArchivo,
                bytes = contenidoFinal.toByteArray(Charsets.UTF_8),
                hashArchivo = calcularHash(contenidoFinal.toByteArray(Charsets.UTF_8)),
                negocioNombre = datos.negocio.nombre
            )
        } catch (e: Exception) {
            ResultadoExportacion.Error(
                mensaje = e.message ?: "No se pudo exportar corte JSON."
            )
        }
    }

    override suspend fun exportarCorteExcel(corteId: String): ResultadoExportacion {
        return try {
            val datos = obtenerDatosBase(corteId)
                ?: return ResultadoExportacion.Error("No se encontró información suficiente para exportar Excel.")

            val nombreArchivo = construirNombreArchivo(
                negocioNombre = datos.negocio.nombre,
                fechaCorte = datos.corte.fechaCorte,
                dispositivoId = datos.dispositivo.id,
                corteId = datos.corte.id,
                tipo = "corte",
                extension = "xlsx"
            )

            val bytesIniciales = generadorExcel.construirCorteExcel(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = null
            )

            val hash = calcularHash(bytesIniciales)

            val bytesFinales = generadorExcel.construirCorteExcel(
                corte = datos.corte,
                ventas = datos.ventas,
                negocio = datos.negocio,
                dispositivo = datos.dispositivo,
                nombreArchivo = nombreArchivo,
                hashArchivo = hash
            )

            guardarArchivoYRegistro(
                corteId = corteId,
                negocioId = datos.corte.negocioId,
                dispositivoId = datos.corte.dispositivoId,
                fechaCorte = datos.corte.fechaCorte,
                tipoArchivo = TipoArchivo.CORTE_EXCEL,
                nombreArchivo = nombreArchivo,
                bytes = bytesFinales,
                hashArchivo = calcularHash(bytesFinales),
                negocioNombre = datos.negocio.nombre
            )
        } catch (e: Exception) {
            ResultadoExportacion.Error(
                mensaje = e.message ?: "No se pudo exportar corte Excel."
            )
        }
    }

    private suspend fun obtenerDatosBase(corteId: String): DatosExportacion? {
        val corte = corteDiarioDao.obtenerPorId(corteId) ?: return null
        val negocio = negocioDao.obtenerPorId(corte.negocioId) ?: return null

        val dispositivo = dispositivoDao.obtenerActual()
            ?: return null

        val ventas = ventaDao.obtenerPorCorteIdYEstado(
            corteId = corte.id,
            estado = VentaEstado.CLOSED
        )

        if (ventas.isEmpty()) return null

        return DatosExportacion(
            corte = corte,
            ventas = ventas,
            negocio = negocio,
            dispositivo = dispositivo
        )
    }

    private suspend fun guardarArchivoYRegistro(
        corteId: String,
        negocioId: String,
        dispositivoId: String,
        fechaCorte: String,
        tipoArchivo: String,
        nombreArchivo: String,
        bytes: ByteArray,
        hashArchivo: String,
        negocioNombre: String
    ): ResultadoExportacion {
        val carpetaDestino = obtenerCarpetaDestino(
            negocioNombre = negocioNombre,
            fechaCorte = fechaCorte
        )

        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs()
        }

        val archivo = File(carpetaDestino, nombreArchivo)
        archivo.writeBytes(bytes)

        val ahora = System.currentTimeMillis()

        val registroExistente = registroArchivoSyncDao.obtenerPorCorte(corteId)
            .firstOrNull { it.tipoArchivo == tipoArchivo }

        val registro = RegistroArchivoSyncEntity(
            id = registroExistente?.id ?: UUID.randomUUID().toString(),
            corteId = corteId,
            negocioId = negocioId,
            dispositivoId = dispositivoId,
            fechaCorte = fechaCorte,
            tipoArchivo = tipoArchivo,
            nombreArchivo = nombreArchivo,
            rutaLocal = archivo.absolutePath,
            hashArchivo = hashArchivo,
            driveFileId = registroExistente?.driveFileId,
            creadoEn = registroExistente?.creadoEn ?: ahora,
            actualizadoEn = ahora,
            sincronizadoEn = registroExistente?.sincronizadoEn,
            syncEstado = SyncEstado.PENDING_SYNC,
            mensajeError = null
        )

        registroArchivoSyncDao.insertar(registro)

        return ResultadoExportacion.Exito(
            archivo = registro.toDomain()
        )
    }

    private fun obtenerCarpetaDestino(
        negocioNombre: String,
        fechaCorte: String
    ): File {
        return File(
            context.filesDir,
            "VentaKing/${normalizarParaArchivo(negocioNombre)}/$fechaCorte"
        )
    }

    private fun construirNombreArchivo(
        negocioNombre: String,
        fechaCorte: String,
        dispositivoId: String,
        corteId: String,
        tipo: String,
        extension: String
    ): String {
        val negocio = normalizarParaArchivo(negocioNombre)
        val dispositivoCorto = normalizarParaArchivo(dispositivoId).take(8)
        val corteCorto = normalizarParaArchivo(corteId).take(8)

        return "${negocio}_${fechaCorte}_${dispositivoCorto}_${corteCorto}_${tipo}.$extension"
    }

    private fun calcularHash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)

        return hash.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }

    private fun normalizarParaArchivo(valor: String): String {
        val sinAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return sinAcentos
            .lowercase()
            .replace("[^a-z0-9_-]+".toRegex(), "_")
            .replace("_+".toRegex(), "_")
            .trim('_')
            .ifBlank { "sin_nombre" }
    }

    private fun RegistroArchivoSyncEntity.toDomain(): ArchivoExportado {
        return ArchivoExportado(
            id = id,
            corteId = corteId,
            negocioId = negocioId,
            dispositivoId = dispositivoId,
            fechaCorte = fechaCorte,
            tipoArchivo = tipoArchivo,
            nombreArchivo = nombreArchivo,
            rutaLocal = rutaLocal,
            hashArchivo = hashArchivo,
            creadoEn = creadoEn,
            syncEstado = syncEstado
        )
    }

    private data class DatosExportacion(
        val corte: com.ventaking.app.datos.local.entidades.CorteDiarioEntity,
        val ventas: List<com.ventaking.app.datos.local.entidades.VentaEntity>,
        val negocio: com.ventaking.app.datos.local.entidades.NegocioEntity,
        val dispositivo: com.ventaking.app.datos.local.entidades.DispositivoEntity
    )

    private object TipoArchivo {
        const val VENTAS_JSON = "VENTAS_JSON"
        const val CORTE_JSON = "CORTE_JSON"
        const val CORTE_EXCEL = "CORTE_EXCEL"
    }
}
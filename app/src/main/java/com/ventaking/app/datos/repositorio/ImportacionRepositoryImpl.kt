package com.ventaking.app.datos.repositorio

import androidx.room.withTransaction
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.HistorialVentaDao
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import com.ventaking.app.dominio.repositorio.ImportacionRepository
import com.ventaking.app.dominio.repositorio.ResultadoImportacion
import org.json.JSONObject
import java.util.UUID

class ImportacionRepositoryImpl(
    private val database: AppDatabase,
    private val negocioDao: NegocioDao,
    private val ventaDao: VentaDao,
    private val corteDiarioDao: CorteDiarioDao,
    private val historialVentaDao: HistorialVentaDao
) : ImportacionRepository {

    override suspend fun importarVentasJson(
        contenidoJson: String
    ): ResultadoImportacion {
        return try {
            val raiz = JSONObject(contenidoJson)

            val tipoArchivo = raiz.optString("tipoArchivo")
            if (tipoArchivo != "VENTAS_JSON") {
                return ResultadoImportacion.Error(
                    mensaje = "El archivo seleccionado no es un respaldo de ventas válido."
                )
            }

            val negocioJson = raiz.getJSONObject("negocio")
            val corteJson = raiz.getJSONObject("corte")
            val ventasJson = raiz.getJSONArray("ventas")

            val nombreNegocioImportado = negocioJson.optString("nombre").trim()
            if (nombreNegocioImportado.isBlank()) {
                return ResultadoImportacion.Error(
                    mensaje = "El archivo no contiene un negocio válido."
                )
            }

            val corteId = corteJson.getString("id")

            val corteExistente = corteDiarioDao.obtenerPorId(corteId)
            if (corteExistente != null) {
                return ResultadoImportacion.Error(
                    mensaje = "Este corte ya existe en este teléfono. No se importó de nuevo."
                )
            }

            if (ventasJson.length() == 0) {
                return ResultadoImportacion.Error(
                    mensaje = "El archivo no contiene ventas para importar."
                )
            }

            var ventasImportadas = 0

            database.withTransaction {
                val negocioLocal = obtenerOCrearNegocioLocal(
                    negocioJson = negocioJson
                )

                val negocioIdDestino = negocioLocal.id

                val corteImportado = CorteDiarioEntity(
                    id = corteId,
                    negocioId = negocioIdDestino,
                    fechaCorte = corteJson.getString("fechaCorte"),
                    dispositivoId = corteJson.getString("dispositivoId"),
                    totalCentavos = corteJson.getLong("totalCentavos"),
                    totalVentas = corteJson.getInt("totalVentas"),
                    totalPiezas = corteJson.getInt("totalPiezas"),
                    creadoEn = corteJson.optLong("creadoEn", System.currentTimeMillis()),
                    cerradoEn = corteJson.optLong("cerradoEn", System.currentTimeMillis()),
                    estado = corteJson.optString("estado", "CLOSED"),
                    syncEstado = corteJson.optString("syncEstado", "SYNCED"),
                    sincronizadoEn = null,
                    mensajeError = null
                )

                corteDiarioDao.insertar(corteImportado)

                val historialImportado = mutableListOf<HistorialVentaEntity>()

                for (indice in 0 until ventasJson.length()) {
                    val ventaJson = ventasJson.getJSONObject(indice)
                    val ventaId = ventaJson.getString("id")

                    val ventaExistente = ventaDao.obtenerPorId(ventaId)
                    if (ventaExistente != null) continue

                    val venta = VentaEntity(
                        id = ventaId,
                        grupoVentaId = ventaJson.getString("grupoVentaId"),
                        negocioId = negocioIdDestino,
                        productoId = ventaJson.optString("productoId").takeIf { it.isNotBlank() && it != "null" },
                        nombreProductoSnapshot = ventaJson.getString("nombreProductoSnapshot"),
                        cantidad = ventaJson.getInt("cantidad"),
                        precioUnitarioSnapshotCentavos = ventaJson.getLong("precioUnitarioSnapshotCentavos"),
                        subtotalCentavos = ventaJson.getLong("subtotalCentavos"),
                        extraCentavos = ventaJson.optLong("extraCentavos", 0L),
                        descuentoCentavos = ventaJson.optLong("descuentoCentavos", 0L),
                        totalCentavos = ventaJson.getLong("totalCentavos"),
                        fechaVenta = ventaJson.getString("fechaVenta"),
                        horaVenta = ventaJson.getString("horaVenta"),
                        creadoEn = ventaJson.optLong("creadoEn", System.currentTimeMillis()),
                        actualizadoEn = System.currentTimeMillis(),
                        dispositivoId = ventaJson.getString("dispositivoId"),
                        corteId = corteId,
                        estado = "CLOSED",
                        syncEstado = ventaJson.optString("syncEstado", "SYNCED")
                    )

                    ventaDao.insertar(venta)

                    historialImportado.add(
                        HistorialVentaEntity(
                            id = UUID.randomUUID().toString(),
                            ventaId = venta.id,
                            grupoVentaId = venta.grupoVentaId,
                            negocioId = negocioIdDestino,
                            dispositivoId = venta.dispositivoId,
                            tipoAccion = "CLOSED",
                            totalAnteriorCentavos = null,
                            totalNuevoCentavos = venta.totalCentavos,
                            cantidadAnterior = null,
                            cantidadNueva = venta.cantidad,
                            nota = "Importado desde respaldo externo.",
                            creadoEn = System.currentTimeMillis(),
                            syncEstado = "LOCAL_ONLY"
                        )
                    )

                    ventasImportadas++
                }

                if (historialImportado.isNotEmpty()) {
                    historialVentaDao.insertarTodos(historialImportado)
                }
            }

            ResultadoImportacion.Exito(
                mensaje = "Corte importado correctamente. Ventas importadas: $ventasImportadas.",
                ventasImportadas = ventasImportadas,
                corteImportadoId = corteId
            )
        } catch (e: Exception) {
            ResultadoImportacion.Error(
                mensaje = e.message ?: "No se pudo importar el archivo JSON."
            )
        }
    }

    private suspend fun obtenerOCrearNegocioLocal(
        negocioJson: JSONObject
    ): NegocioEntity {
        val nombre = negocioJson.optString("nombre").trim()

        val negocioExistentePorNombre = negocioDao.obtenerPorNombre(nombre)
        if (negocioExistentePorNombre != null) {
            return negocioExistentePorNombre
        }

        val ahora = System.currentTimeMillis()

        val nuevoNegocio = NegocioEntity(
            id = negocioJson.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            nombre = nombre,
            estaActivo = true,
            creadoEn = ahora,
            actualizadoEn = ahora
        )

        negocioDao.insertar(nuevoNegocio)

        return nuevoNegocio
    }
}
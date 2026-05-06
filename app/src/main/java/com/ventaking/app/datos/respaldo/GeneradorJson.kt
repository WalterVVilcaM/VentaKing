package com.ventaking.app.datos.respaldo

import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import org.json.JSONArray
import org.json.JSONObject

class GeneradorJson {

    fun construirVentasJson(
        corte: CorteDiarioEntity,
        ventas: List<VentaEntity>,
        negocio: NegocioEntity,
        dispositivo: DispositivoEntity,
        nombreArchivo: String,
        hashArchivo: String?
    ): String {
        val ventasArray = JSONArray()

        ventas.forEach { venta ->
            ventasArray.put(
                JSONObject()
                    .put("id", venta.id)
                    .put("grupoVentaId", venta.grupoVentaId)
                    .put("negocioId", venta.negocioId)
                    .put("productoId", venta.productoId)
                    .put("nombreProductoSnapshot", venta.nombreProductoSnapshot)
                    .put("cantidad", venta.cantidad)
                    .put("precioUnitarioSnapshotCentavos", venta.precioUnitarioSnapshotCentavos)
                    .put("subtotalCentavos", venta.subtotalCentavos)
                    .put("extraCentavos", venta.extraCentavos)
                    .put("descuentoCentavos", venta.descuentoCentavos)
                    .put("totalCentavos", venta.totalCentavos)
                    .put("fechaVenta", venta.fechaVenta)
                    .put("horaVenta", venta.horaVenta)
                    .put("creadoEn", venta.creadoEn)
                    .put("actualizadoEn", venta.actualizadoEn)
                    .put("dispositivoId", venta.dispositivoId)
                    .put("corteId", venta.corteId)
                    .put("estado", venta.estado)
                    .put("syncEstado", venta.syncEstado)
            )
        }

        return JSONObject()
            .put("tipoArchivo", "VENTAS_JSON")
            .put("nombreArchivo", nombreArchivo)
            .put("hashArchivo", hashArchivo)
            .put("generadoEn", System.currentTimeMillis())
            .put(
                "negocio",
                JSONObject()
                    .put("id", negocio.id)
                    .put("nombre", negocio.nombre)
            )
            .put(
                "dispositivo",
                JSONObject()
                    .put("id", dispositivo.id)
                    .put("nombreDispositivo", dispositivo.nombreDispositivo)
            )
            .put(
                "corte",
                JSONObject()
                    .put("id", corte.id)
                    .put("negocioId", corte.negocioId)
                    .put("fechaCorte", corte.fechaCorte)
                    .put("dispositivoId", corte.dispositivoId)
                    .put("totalCentavos", corte.totalCentavos)
                    .put("totalVentas", corte.totalVentas)
                    .put("totalPiezas", corte.totalPiezas)
                    .put("creadoEn", corte.creadoEn)
                    .put("cerradoEn", corte.cerradoEn)
                    .put("estado", corte.estado)
                    .put("syncEstado", corte.syncEstado)
            )
            .put("ventas", ventasArray)
            .toString(2)
    }

    fun construirCorteJson(
        corte: CorteDiarioEntity,
        ventas: List<VentaEntity>,
        negocio: NegocioEntity,
        dispositivo: DispositivoEntity,
        nombreArchivo: String,
        hashArchivo: String?
    ): String {
        val resumenProductos = JSONArray()

        ventas
            .groupBy { it.nombreProductoSnapshot }
            .toSortedMap()
            .forEach { (nombreProducto, ventasProducto) ->
                resumenProductos.put(
                    JSONObject()
                        .put("producto", nombreProducto)
                        .put("cantidadTotal", ventasProducto.sumOf { it.cantidad })
                        .put("totalCentavos", ventasProducto.sumOf { it.totalCentavos })
                )
            }

        return JSONObject()
            .put("tipoArchivo", "CORTE_JSON")
            .put("nombreArchivo", nombreArchivo)
            .put("hashArchivo", hashArchivo)
            .put("generadoEn", System.currentTimeMillis())
            .put(
                "negocio",
                JSONObject()
                    .put("id", negocio.id)
                    .put("nombre", negocio.nombre)
            )
            .put(
                "dispositivo",
                JSONObject()
                    .put("id", dispositivo.id)
                    .put("nombreDispositivo", dispositivo.nombreDispositivo)
            )
            .put(
                "corte",
                JSONObject()
                    .put("id", corte.id)
                    .put("fechaCorte", corte.fechaCorte)
                    .put("totalCentavos", corte.totalCentavos)
                    .put("totalVentas", corte.totalVentas)
                    .put("totalPiezas", corte.totalPiezas)
                    .put("creadoEn", corte.creadoEn)
                    .put("cerradoEn", corte.cerradoEn)
                    .put("estado", corte.estado)
                    .put("syncEstado", corte.syncEstado)
            )
            .put("resumenProductos", resumenProductos)
            .toString(2)
    }
}
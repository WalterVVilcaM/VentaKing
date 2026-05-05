package com.ventaking.app.dominio.modelo

data class Venta(
    val id: String,
    val grupoVentaId: String,
    val negocioId: String,
    val productoId: String?,
    val nombreProductoSnapshot: String,
    val cantidad: Int,
    val precioUnitarioSnapshotCentavos: Long,
    val subtotalCentavos: Long,
    val extraCentavos: Long,
    val descuentoCentavos: Long,
    val totalCentavos: Long,
    val fechaVenta: String,
    val horaVenta: String,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val dispositivoId: String,
    val corteId: String?,
    val estado: String,
    val syncEstado: String
) {
    val totalComoDecimal: Double
        get() = totalCentavos / 100.0

    val subtotalComoDecimal: Double
        get() = subtotalCentavos / 100.0
}
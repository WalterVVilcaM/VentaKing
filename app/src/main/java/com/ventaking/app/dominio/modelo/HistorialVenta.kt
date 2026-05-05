package com.ventaking.app.dominio.modelo

data class HistorialVenta(
    val id: String,
    val ventaId: String,
    val grupoVentaId: String,
    val negocioId: String,
    val dispositivoId: String,
    val tipoAccion: String,
    val totalAnteriorCentavos: Long?,
    val totalNuevoCentavos: Long?,
    val cantidadAnterior: Int?,
    val cantidadNueva: Int?,
    val nota: String?,
    val creadoEn: Long,
    val syncEstado: String
)
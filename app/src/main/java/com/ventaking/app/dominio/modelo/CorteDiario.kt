package com.ventaking.app.dominio.modelo

data class CorteDiario(
    val id: String,
    val negocioId: String,
    val fechaCorte: String,
    val dispositivoId: String,
    val totalCentavos: Long,
    val totalVentas: Int,
    val totalPiezas: Int,
    val creadoEn: Long,
    val cerradoEn: Long,
    val estado: String,
    val syncEstado: String,
    val sincronizadoEn: Long?,
    val mensajeError: String?
) {
    val totalComoDecimal: Double
        get() = totalCentavos / 100.0
}
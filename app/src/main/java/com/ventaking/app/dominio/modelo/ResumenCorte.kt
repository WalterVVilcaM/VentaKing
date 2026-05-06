package com.ventaking.app.dominio.modelo

data class ResumenCorte(
    val negocioId: String,
    val fechaCorte: String,
    val dispositivoId: String,
    val ventas: List<Venta>,
    val corteExistente: CorteDiario?
) {
    val totalCentavos: Long
        get() = ventas.sumOf { it.totalCentavos }

    val totalVentas: Int
        get() = ventas.map { it.grupoVentaId }.distinct().size

    val totalPiezas: Int
        get() = ventas.sumOf { it.cantidad }

    val tieneVentasParaCorte: Boolean
        get() = ventas.isNotEmpty()

    val yaTieneCorte: Boolean
        get() = corteExistente != null
}
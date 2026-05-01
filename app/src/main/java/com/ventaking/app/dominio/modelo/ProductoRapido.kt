package com.ventaking.app.dominio.modelo

data class ProductoRapido(
    val id: String,
    val negocioId: String,
    val nombre: String,
    val precioCentavos: Long,
    val categoria: String?,
    val esPromocion: Boolean,
    val estaActivo: Boolean,
    val creadoEn: Long,
    val actualizadoEn: Long
) {
    val precioComoDecimal: Double
        get() = precioCentavos / 100.0
}
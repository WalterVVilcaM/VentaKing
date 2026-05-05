package com.ventaking.app.presentacion.pantallas.venta

import com.ventaking.app.dominio.modelo.ProductoRapido

data class CartItem(
    val producto: ProductoRapido,
    val cantidad: Int
) {
    val totalItemCentavos: Long
        get() = producto.precioCentavos * cantidad
}

data class VentaUiState(
    val cargando: Boolean = true,
    val negocios: List<NegocioVentaUi> = emptyList(),
    val negocioSeleccionadoId: String? = null,
    val productos: List<ProductoRapido> = emptyList(),

    val carrito: List<CartItem> = emptyList(),

    val extraTexto: String = "",
    val descuentoTexto: String = "",

    val subtotalCentavos: Long = 0L,
    val totalCentavos: Long = 0L,
    val totalDiaCentavos: Long = 0L,

    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val guardando: Boolean = false
)

data class NegocioVentaUi(
    val id: String,
    val nombre: String
)
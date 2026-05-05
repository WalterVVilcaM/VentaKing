package com.ventaking.app.presentacion.pantallas.ventas_dia

data class VentasDiaUiState(
    val cargando: Boolean = true,
    val negocios: List<NegocioVentasDiaUi> = emptyList(),
    val negocioSeleccionadoId: String? = null,
    val fechaActual: String = "",

    // Lista plana interna, útil para cálculos o futuras consultas.
    val ventas: List<VentaDiaUi> = emptyList(),

    // Lista real que debe pintar la pantalla.
    val grupos: List<GrupoVentaDiaUi> = emptyList(),

    val totalDiaCentavos: Long = 0L,

    // Ahora representa transacciones/grupos, no renglones individuales.
    val cantidadVentas: Int = 0,

    val piezasVendidas: Int = 0,

    val ventaEnEdicion: VentaDiaUi? = null,

    val ventaParaCancelar: VentaDiaUi? = null,

    // Cancelación ahora es por transacción completa.
    val grupoParaCancelar: GrupoVentaDiaUi? = null,

    val editando: Boolean = false,
    val cancelando: Boolean = false,

    val cantidadTexto: String = "",
    val extraTexto: String = "",
    val descuentoTexto: String = "",
    val notaTexto: String = "",

    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

data class NegocioVentasDiaUi(
    val id: String,
    val nombre: String
)

data class GrupoVentaDiaUi(
    val grupoVentaId: String,
    val ventas: List<VentaDiaUi>,
    val horaVenta: String,
    val totalCentavos: Long,
    val piezas: Int,
    val productosDistintos: Int,
    val resumenProductos: String,
    val tieneCorte: Boolean
)

data class VentaDiaUi(
    val id: String,
    val grupoVentaId: String,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitarioCentavos: Long,
    val subtotalCentavos: Long,
    val extraCentavos: Long,
    val descuentoCentavos: Long,
    val totalCentavos: Long,
    val horaVenta: String,
    val estado: String,
    val corteId: String?
)
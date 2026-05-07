package com.ventaking.app.presentacion.pantallas.corte

data class CorteUiState(
    val cargando: Boolean = true,
    val creandoCorte: Boolean = false,
    val negocios: List<NegocioCorteUi> = emptyList(),
    val negocioSeleccionadoId: String? = null,
    val fechaActual: String = "",
    val dispositivoId: String? = null,
    val totalCentavos: Long = 0L,
    val totalVentas: Int = 0,
    val totalPiezas: Int = 0,
    val corteExistenteId: String? = null,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val mostrarConfirmacionCorte: Boolean = false,
    val mostrarModalSinInternet: Boolean = false,
    val mensajeModalSinInternet: String? = null
) {
    val puedeCrearCorte: Boolean
        get() = !cargando &&
                !creandoCorte &&
                negocioSeleccionadoId != null &&
                dispositivoId != null &&
                totalVentas > 0 &&
                corteExistenteId == null
}

data class NegocioCorteUi(
    val id: String,
    val nombre: String
)
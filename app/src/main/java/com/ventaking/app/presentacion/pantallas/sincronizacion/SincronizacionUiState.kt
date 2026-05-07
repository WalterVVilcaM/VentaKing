package com.ventaking.app.presentacion.pantallas.sincronizacion

data class SincronizacionUiState(
    val cargando: Boolean = true,
    val cortes: List<CortePendienteSyncUi> = emptyList(),
    val reintentandoCorteId: String? = null,
    val reintentandoTodos: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
) {
    val hayPendientes: Boolean
        get() = cortes.isNotEmpty()
}

data class CortePendienteSyncUi(
    val id: String,
    val negocioId: String,
    val fechaCorte: String,
    val dispositivoId: String,
    val totalCentavos: Long,
    val totalVentas: Int,
    val totalPiezas: Int,
    val syncEstado: String,
    val mensajeError: String?
)
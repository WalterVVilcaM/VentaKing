package com.ventaking.app.presentacion.pantallas.historial

import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.Dispositivo
import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Negocio
import com.ventaking.app.dominio.modelo.Venta

data class HistorialUiState(
    val cargando: Boolean = true,
    val mensajeError: String? = null,
    val mensajeAccion: String? = null,

    val fechaSeleccionada: String = "",

    val negocios: List<Negocio> = emptyList(),
    val negocioSeleccionadoId: String? = null,

    val dispositivoActual: Dispositivo? = null,

    val origenSeleccionado: OrigenDatosFiltro = OrigenDatosFiltro.ESTE_DISPOSITIVO,

    val estadosDisponibles: List<EstadoVentaFiltro> = EstadoVentaFiltro.entries,
    val estadoSeleccionado: EstadoVentaFiltro = EstadoVentaFiltro.TODAS,

    val ventas: List<Venta> = emptyList(),
    val gruposVenta: List<GrupoVentaHistorialUi> = emptyList(),

    val cortes: List<CorteDiario> = emptyList(),
    val corteSeleccionadoId: String? = null,

    val movimientosDelDia: List<HistorialVenta> = emptyList(),

    val grupoSeleccionadoId: String? = null,
    val historialGrupoSeleccionado: List<HistorialVenta> = emptyList()
) {
    val negocioSeleccionado: Negocio?
        get() = negocios.firstOrNull { it.id == negocioSeleccionadoId }

    val dispositivoFiltroId: String?
        get() = when (origenSeleccionado) {
            OrigenDatosFiltro.ESTE_DISPOSITIVO -> dispositivoActual?.id
            OrigenDatosFiltro.TODOS_LOCALES -> null
        }

    val hayNegocios: Boolean
        get() = negocios.isNotEmpty()

    val hayVentas: Boolean
        get() = ventas.isNotEmpty()

    val hayCortes: Boolean
        get() = cortes.isNotEmpty()

    val corteSeleccionado: CorteDiario?
        get() = cortes.firstOrNull { it.id == corteSeleccionadoId }
            ?: cortes.firstOrNull()

    val grupoSeleccionado: GrupoVentaHistorialUi?
        get() = gruposVenta.firstOrNull { it.grupoVentaId == grupoSeleccionadoId }

    val totalVentasCentavos: Long
        get() = ventas
            .filter { it.estado != "CANCELLED" }
            .sumOf { it.totalCentavos }

    val totalPiezas: Int
        get() = ventas
            .filter { it.estado != "CANCELLED" }
            .sumOf { it.cantidad }

    val totalVentasVisibles: Int
        get() = ventas.size

    val totalCortes: Int
        get() = cortes.size

    val totalMovimientos: Int
        get() = movimientosDelDia.size

    val descripcionOrigen: String
        get() = when (origenSeleccionado) {
            OrigenDatosFiltro.ESTE_DISPOSITIVO -> "Solo datos guardados por este teléfono."
            OrigenDatosFiltro.TODOS_LOCALES -> "Datos locales disponibles, incluyendo importados."
        }
}

enum class OrigenDatosFiltro(
    val etiqueta: String
) {
    ESTE_DISPOSITIVO(
        etiqueta = "Este dispositivo"
    ),
    TODOS_LOCALES(
        etiqueta = "Todos locales"
    )
}

enum class EstadoVentaFiltro(
    val etiqueta: String,
    val valor: String?
) {
    TODAS(
        etiqueta = "Todas",
        valor = null
    ),
    ACTIVE(
        etiqueta = "Activas",
        valor = "ACTIVE"
    ),
    CANCELLED(
        etiqueta = "Canceladas",
        valor = "CANCELLED"
    ),
    CLOSED(
        etiqueta = "Cerradas",
        valor = "CLOSED"
    )
}

data class GrupoVentaHistorialUi(
    val grupoVentaId: String,
    val ventas: List<Venta>
) {
    val hora: String
        get() = ventas.minByOrNull { it.creadoEn }?.horaVenta.orEmpty()

    val totalCentavos: Long
        get() = ventas.sumOf { it.totalCentavos }

    val totalPiezas: Int
        get() = ventas.sumOf { it.cantidad }

    val productosTexto: String
        get() = ventas.joinToString(separator = ", ") { venta ->
            "${venta.cantidad} x ${venta.nombreProductoSnapshot}"
        }

    val corteId: String?
        get() = ventas.firstOrNull { !it.corteId.isNullOrBlank() }?.corteId

    val estadoResumen: String
        get() {
            val estados = ventas.map { it.estado }.distinct()

            return when {
                estados.isEmpty() -> "SIN ESTADO"
                estados.size == 1 -> estados.first()
                estados.contains("ACTIVE") -> "ACTIVE"
                estados.contains("CLOSED") && estados.contains("CANCELLED") -> "MIXTO"
                else -> estados.first()
            }
        }

    val syncEstadoResumen: String
        get() {
            val estados = ventas.map { it.syncEstado }.distinct()
            return when {
                estados.isEmpty() -> "LOCAL_ONLY"
                estados.size == 1 -> estados.first()
                estados.contains("SYNC_ERROR") -> "SYNC_ERROR"
                estados.contains("PENDING_SYNC") -> "PENDING_SYNC"
                estados.contains("SYNCED") -> "SYNCED"
                else -> estados.first()
            }
        }

    val estaCerrado: Boolean
        get() = estadoResumen == "CLOSED"

    val estaCancelado: Boolean
        get() = estadoResumen == "CANCELLED"

    val estaActivo: Boolean
        get() = estadoResumen == "ACTIVE"
}
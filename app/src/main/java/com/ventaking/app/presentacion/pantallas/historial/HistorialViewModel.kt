package com.ventaking.app.presentacion.pantallas.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.dominio.casos.historial.ObtenerCortesHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerDispositivoActualHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerHistorialGrupoVentaUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerMovimientosHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerNegociosHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerVentasHistorialUseCase
import com.ventaking.app.dominio.modelo.Venta
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.ventaking.app.dominio.casos.historial.ImportarVentasJsonUseCase
import com.ventaking.app.dominio.repositorio.ResultadoImportacion

class HistorialViewModel(
    private val obtenerVentasHistorialUseCase: ObtenerVentasHistorialUseCase,
    private val obtenerCortesHistorialUseCase: ObtenerCortesHistorialUseCase,
    private val obtenerHistorialGrupoVentaUseCase: ObtenerHistorialGrupoVentaUseCase,
    private val obtenerMovimientosHistorialUseCase: ObtenerMovimientosHistorialUseCase,
    private val obtenerNegociosHistorialUseCase: ObtenerNegociosHistorialUseCase,
    private val obtenerDispositivoActualHistorialUseCase: ObtenerDispositivoActualHistorialUseCase,
    private val importarVentasJsonUseCase: ImportarVentasJsonUseCase
) : ViewModel() {

    private val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(
        HistorialUiState(
            fechaSeleccionada = LocalDate.now().format(formatoFecha)
        )
    )
    val uiState: StateFlow<HistorialUiState> = _uiState

    private var filtrosJob: Job? = null
    private var historialGrupoJob: Job? = null

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            combine(
                obtenerNegociosHistorialUseCase(),
                obtenerDispositivoActualHistorialUseCase()
            ) { negocios, dispositivo ->
                negocios to dispositivo
            }.collect { (negocios, dispositivo) ->
                val negocioActual = _uiState.value.negocioSeleccionadoId
                val negocioSeleccionado = when {
                    negocioActual != null && negocios.any { it.id == negocioActual } -> negocioActual
                    negocios.isNotEmpty() -> negocios.first().id
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        negocios = negocios,
                        dispositivoActual = dispositivo,
                        negocioSeleccionadoId = negocioSeleccionado,
                        mensajeError = if (negocios.isEmpty()) {
                            "No hay negocios registrados para consultar historial."
                        } else {
                            null
                        }
                    )
                }

                observarDatosConFiltros()
            }
        }
    }

    fun seleccionarNegocio(negocioId: String) {
        if (_uiState.value.negocioSeleccionadoId == negocioId) return

        _uiState.update {
            it.copy(
                negocioSeleccionadoId = negocioId,
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }

    fun seleccionarEstado(estado: EstadoVentaFiltro) {
        if (_uiState.value.estadoSeleccionado == estado) return

        _uiState.update {
            it.copy(
                estadoSeleccionado = estado,
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }

    fun seleccionarOrigen(origen: OrigenDatosFiltro) {
        if (_uiState.value.origenSeleccionado == origen) return

        _uiState.update {
            it.copy(
                origenSeleccionado = origen,
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }

    fun seleccionarCorte(corteId: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                corteSeleccionadoId = if (estadoActual.corteSeleccionadoId == corteId) {
                    null
                } else {
                    corteId
                },
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }
    }
    fun irAlDiaAnterior() {
        cambiarFechaPorDias(-1)
    }

    fun irAlDiaSiguiente() {
        cambiarFechaPorDias(1)
    }

    fun irAHoy() {
        _uiState.update {
            it.copy(
                fechaSeleccionada = LocalDate.now().format(formatoFecha),
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }

    fun seleccionarGrupoVenta(grupoVentaId: String) {
        if (_uiState.value.grupoSeleccionadoId == grupoVentaId) {
            _uiState.update {
                it.copy(
                    grupoSeleccionadoId = null,
                    historialGrupoSeleccionado = emptyList()
                )
            }
            historialGrupoJob?.cancel()
            return
        }

        _uiState.update {
            it.copy(
                grupoSeleccionadoId = grupoVentaId,
                historialGrupoSeleccionado = emptyList()
            )
        }

        historialGrupoJob?.cancel()
        historialGrupoJob = viewModelScope.launch {
            obtenerHistorialGrupoVentaUseCase(grupoVentaId).collect { movimientos ->
                _uiState.update {
                    it.copy(historialGrupoSeleccionado = movimientos)
                }
            }
        }
    }

    fun mostrarSelectorImportacion() {
        _uiState.update {
            it.copy(
                mensajeAccion = "Selecciona un archivo ventas.json generado por VentaKing."
            )
        }
    }

    fun solicitarExportarCorte() {
        val corte = _uiState.value.corteSeleccionado

        if (corte == null) {
            _uiState.update {
                it.copy(
                    mensajeAccion = "No hay corte disponible para exportar con los filtros actuales."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                mensajeAccion = "Exportación preparada para el corte del ${corte.fechaCorte}. Se conectará en el siguiente bloque."
            )
        }
    }

    fun limpiarMensajeError() {
        _uiState.update {
            it.copy(mensajeError = null)
        }
    }

    fun limpiarMensajeAccion() {
        _uiState.update {
            it.copy(mensajeAccion = null)
        }
    }

    fun importarVentasJsonDesdeContenido(
        contenidoJson: String
    ) {
        viewModelScope.launch {
            when (val resultado = importarVentasJsonUseCase(contenidoJson)) {
                is ResultadoImportacion.Exito -> {
                    _uiState.update {
                        it.copy(
                            mensajeAccion = resultado.mensaje,
                            origenSeleccionado = OrigenDatosFiltro.TODOS_LOCALES,
                            corteSeleccionadoId = resultado.corteImportadoId,
                            grupoSeleccionadoId = null,
                            historialGrupoSeleccionado = emptyList()
                        )
                    }

                    observarDatosConFiltros()
                }

                is ResultadoImportacion.Error -> {
                    _uiState.update {
                        it.copy(
                            mensajeAccion = resultado.mensaje
                        )
                    }
                }
            }
        }
    }

    fun mostrarMensajeAccion(mensaje: String) {
        _uiState.update {
            it.copy(
                mensajeAccion = mensaje
            )
        }
    }

    private fun cambiarFechaPorDias(dias: Long) {
        val fechaActual = obtenerFechaActualComoLocalDate()

        _uiState.update {
            it.copy(
                fechaSeleccionada = fechaActual.plusDays(dias).format(formatoFecha),
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }

    private fun observarDatosConFiltros() {
        val estadoActual = _uiState.value
        val negocioId = estadoActual.negocioSeleccionadoId ?: return
        val fecha = estadoActual.fechaSeleccionada
        val dispositivoId = estadoActual.dispositivoFiltroId
        val estadoVenta = estadoActual.estadoSeleccionado.valor
        val rango = obtenerRangoTimestampDelDia(fecha)

        filtrosJob?.cancel()
        filtrosJob = viewModelScope.launch {
            combine(
                obtenerVentasHistorialUseCase(
                    negocioId = negocioId,
                    fechaVenta = fecha,
                    dispositivoId = dispositivoId,
                    estado = estadoVenta
                ),
                obtenerCortesHistorialUseCase(
                    negocioId = negocioId,
                    fechaCorte = fecha,
                    dispositivoId = dispositivoId
                ),
                obtenerMovimientosHistorialUseCase(
                    negocioId = negocioId,
                    dispositivoId = dispositivoId,
                    desde = rango.first,
                    hasta = rango.second
                )
            ) { ventas, cortes, movimientos ->
                Triple(ventas, cortes, movimientos)
            }.collect { (ventas, cortes, movimientos) ->
                val corteSeleccionadoActual = _uiState.value.corteSeleccionadoId
                val nuevoCorteSeleccionado = when {
                    corteSeleccionadoActual != null && cortes.any { it.id == corteSeleccionadoActual } -> {
                        corteSeleccionadoActual
                    }
                    else -> null
                }
                _uiState.update {
                    it.copy(
                        ventas = ventas,
                        gruposVenta = agruparVentas(ventas),
                        cortes = cortes,
                        corteSeleccionadoId = nuevoCorteSeleccionado,
                        movimientosDelDia = movimientos,
                        mensajeError = null
                    )
                }
            }
        }
    }

    private fun agruparVentas(ventas: List<Venta>): List<GrupoVentaHistorialUi> {
        return ventas
            .groupBy { it.grupoVentaId }
            .map { (_, ventasDelGrupo) ->
                GrupoVentaHistorialUi(
                    grupoVentaId = ventasDelGrupo.first().grupoVentaId,
                    ventas = ventasDelGrupo.sortedBy { it.creadoEn }
                )
            }
            .sortedByDescending { grupo ->
                grupo.ventas.maxOfOrNull { it.creadoEn } ?: 0L
            }
    }

    private fun obtenerFechaActualComoLocalDate(): LocalDate {
        return try {
            LocalDate.parse(_uiState.value.fechaSeleccionada, formatoFecha)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    private fun obtenerRangoTimestampDelDia(fechaTexto: String): Pair<Long, Long> {
        val fecha = try {
            LocalDate.parse(fechaTexto, formatoFecha)
        } catch (_: Exception) {
            LocalDate.now()
        }

        val zona = ZoneId.systemDefault()

        val inicio = fecha
            .atStartOfDay(zona)
            .toInstant()
            .toEpochMilli()

        val fin = fecha
            .atTime(LocalTime.MAX)
            .atZone(zona)
            .toInstant()
            .toEpochMilli()

        return inicio to fin
    }

    fun seleccionarFecha(fecha: String) {
        if (_uiState.value.fechaSeleccionada == fecha) return

        _uiState.update {
            it.copy(
                fechaSeleccionada = fecha,
                corteSeleccionadoId = null,
                grupoSeleccionadoId = null,
                historialGrupoSeleccionado = emptyList()
            )
        }

        observarDatosConFiltros()
    }
}
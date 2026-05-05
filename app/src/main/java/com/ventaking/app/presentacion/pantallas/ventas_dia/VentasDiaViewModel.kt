package com.ventaking.app.presentacion.pantallas.ventas_dia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.dominio.casos.ventas.CancelarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.EditarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.ObtenerVentasDelDiaUseCase
import com.ventaking.app.dominio.casos.ventas.ResultadoCancelarVenta
import com.ventaking.app.dominio.casos.ventas.ResultadoEditarVenta
import com.ventaking.app.dominio.modelo.Venta
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

class VentasDiaViewModel(
    private val negocioDao: NegocioDao,
    private val obtenerVentasDelDiaUseCase: ObtenerVentasDelDiaUseCase,
    private val editarVentaUseCase: EditarVentaUseCase,
    private val cancelarVentaUseCase: CancelarVentaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VentasDiaUiState(
            fechaActual = obtenerFechaActual()
        )
    )
    val uiState: StateFlow<VentasDiaUiState> = _uiState.asStateFlow()

    private var ventasJob: Job? = null

    init {
        observarNegocios()
    }

    private fun observarNegocios() {
        viewModelScope.launch {
            negocioDao.observarActivos().collect { negociosEntity ->
                val negocios = negociosEntity.map { negocio ->
                    NegocioVentasDiaUi(
                        id = negocio.id,
                        nombre = negocio.nombre
                    )
                }

                val negocioActual = _uiState.value.negocioSeleccionadoId

                val negocioSeleccionado = when {
                    negocioActual != null && negocios.any { it.id == negocioActual } -> negocioActual
                    negocios.isNotEmpty() -> negocios.first().id
                    else -> null
                }

                _uiState.update { estado ->
                    estado.copy(
                        negocios = negocios,
                        negocioSeleccionadoId = negocioSeleccionado,
                        cargando = false,
                        mensajeError = if (negocios.isEmpty()) {
                            "No hay negocios activos registrados."
                        } else {
                            estado.mensajeError
                        }
                    )
                }

                if (negocioSeleccionado != null && negocioSeleccionado != negocioActual) {
                    observarVentasDelDia(negocioSeleccionado)
                }
            }
        }
    }

    fun seleccionarNegocio(negocioId: String) {
        val negocioActual = _uiState.value.negocioSeleccionadoId
        if (negocioId == negocioActual) return

        _uiState.update { estado ->
            estado.copy(
                negocioSeleccionadoId = negocioId,
                ventas = emptyList(),
                grupos = emptyList(),
                totalDiaCentavos = 0L,
                cantidadVentas = 0,
                piezasVendidas = 0,
                ventaEnEdicion = null,
                ventaParaCancelar = null,
                grupoParaCancelar = null,
                mensajeError = null,
                mensajeExito = null
            )
        }

        observarVentasDelDia(negocioId)
    }

    private fun observarVentasDelDia(negocioId: String) {
        ventasJob?.cancel()

        ventasJob = viewModelScope.launch {
            obtenerVentasDelDiaUseCase(
                negocioId = negocioId,
                fechaVenta = _uiState.value.fechaActual
            ).collect { ventas ->
                val ventasUi = ventas.map { it.toUi() }
                val gruposUi = agruparVentasPorTransaccion(ventasUi)

                _uiState.update { estado ->
                    estado.copy(
                        ventas = ventasUi,
                        grupos = gruposUi,
                        totalDiaCentavos = ventasUi.sumOf { it.totalCentavos },
                        cantidadVentas = gruposUi.size,
                        piezasVendidas = ventasUi.sumOf { it.cantidad },
                        cargando = false
                    )
                }
            }
        }
    }

    fun abrirEdicion(venta: VentaDiaUi) {
        if (venta.corteId != null) {
            _uiState.update { estado ->
                estado.copy(
                    mensajeError = "No se puede editar una venta que ya pertenece a un corte.",
                    mensajeExito = null
                )
            }
            return
        }

        _uiState.update { estado ->
            estado.copy(
                ventaEnEdicion = venta,
                cantidadTexto = venta.cantidad.toString(),
                extraTexto = centavosATexto(venta.extraCentavos),
                descuentoTexto = centavosATexto(venta.descuentoCentavos),
                notaTexto = "",
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cerrarEdicion() {
        _uiState.update { estado ->
            estado.copy(
                ventaEnEdicion = null,
                editando = false,
                cantidadTexto = "",
                extraTexto = "",
                descuentoTexto = "",
                notaTexto = "",
                mensajeError = null
            )
        }
    }

    fun cambiarCantidadTexto(texto: String) {
        if (texto.isBlank()) {
            _uiState.update { it.copy(cantidadTexto = texto, mensajeError = null) }
            return
        }

        if (!texto.all { it.isDigit() }) return
        if (texto.length > 3) return

        _uiState.update { estado ->
            estado.copy(
                cantidadTexto = texto,
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cambiarExtraTexto(texto: String) {
        if (!esMontoValidoParaTexto(texto)) return

        _uiState.update { estado ->
            estado.copy(
                extraTexto = texto,
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cambiarDescuentoTexto(texto: String) {
        if (!esMontoValidoParaTexto(texto)) return

        _uiState.update { estado ->
            estado.copy(
                descuentoTexto = texto,
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cambiarNotaTexto(texto: String) {
        if (texto.length > 120) return

        _uiState.update { estado ->
            estado.copy(
                notaTexto = texto,
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun guardarEdicion() {
        val estado = _uiState.value
        val venta = estado.ventaEnEdicion ?: return

        if (estado.editando) return

        val cantidad = estado.cantidadTexto.toIntOrNull()
        if (cantidad == null || cantidad < 1) {
            _uiState.update { actual ->
                actual.copy(
                    mensajeError = "La cantidad debe ser mayor a cero.",
                    mensajeExito = null
                )
            }
            return
        }

        val extraCentavos = convertirTextoACentavos(estado.extraTexto)
        val descuentoCentavos = convertirTextoACentavos(estado.descuentoTexto)

        viewModelScope.launch {
            _uiState.update { actual ->
                actual.copy(
                    editando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            val resultado = editarVentaUseCase(
                ventaId = venta.id,
                nuevaCantidad = cantidad,
                nuevoExtraCentavos = extraCentavos,
                nuevoDescuentoCentavos = descuentoCentavos,
                nota = estado.notaTexto
            )

            when (resultado) {
                is ResultadoEditarVenta.Exito -> {
                    delay(700)

                    _uiState.update { actual ->
                        actual.copy(
                            ventaEnEdicion = null,
                            editando = false,
                            cantidadTexto = "",
                            extraTexto = "",
                            descuentoTexto = "",
                            notaTexto = "",
                            mensajeExito = "Producto actualizado dentro de la venta.",
                            mensajeError = null
                        )
                    }
                }

                is ResultadoEditarVenta.Error -> {
                    _uiState.update { actual ->
                        actual.copy(
                            editando = false,
                            mensajeError = resultado.mensaje,
                            mensajeExito = null
                        )
                    }
                }
            }
        }
    }

    fun abrirConfirmacionCancelacion(grupo: GrupoVentaDiaUi) {
        if (grupo.tieneCorte) {
            _uiState.update { estado ->
                estado.copy(
                    mensajeError = "No se puede cancelar una transacción que ya pertenece a un corte.",
                    mensajeExito = null
                )
            }
            return
        }

        _uiState.update { estado ->
            estado.copy(
                grupoParaCancelar = grupo,
                notaTexto = "",
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cerrarConfirmacionCancelacion() {
        _uiState.update { estado ->
            estado.copy(
                grupoParaCancelar = null,
                cancelando = false,
                notaTexto = "",
                mensajeError = null
            )
        }
    }

    fun cancelarGrupoSeleccionado() {
        val estado = _uiState.value
        val grupo = estado.grupoParaCancelar ?: return

        if (estado.cancelando) return

        viewModelScope.launch {
            _uiState.update { actual ->
                actual.copy(
                    cancelando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            var mensajeError: String? = null

            for (venta in grupo.ventas) {
                val resultado = cancelarVentaUseCase(
                    ventaId = venta.id,
                    nota = estado.notaTexto.ifBlank {
                        "Transacción cancelada antes del corte."
                    }
                )

                if (resultado is ResultadoCancelarVenta.Error) {
                    mensajeError = resultado.mensaje
                    break
                }
            }

            if (mensajeError == null) {
                delay(700)

                _uiState.update { actual ->
                    actual.copy(
                        grupoParaCancelar = null,
                        cancelando = false,
                        notaTexto = "",
                        mensajeExito = "Transacción cancelada correctamente.",
                        mensajeError = null
                    )
                }
            } else {
                _uiState.update { actual ->
                    actual.copy(
                        cancelando = false,
                        mensajeError = mensajeError,
                        mensajeExito = null
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { estado ->
            estado.copy(
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun formatearCentavos(centavos: Long): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(centavos / 100.0)
    }

    private fun Venta.toUi(): VentaDiaUi {
        return VentaDiaUi(
            id = id,
            grupoVentaId = grupoVentaId,
            nombreProducto = nombreProductoSnapshot,
            cantidad = cantidad,
            precioUnitarioCentavos = precioUnitarioSnapshotCentavos,
            subtotalCentavos = subtotalCentavos,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos,
            totalCentavos = totalCentavos,
            horaVenta = horaVenta,
            estado = estado,
            corteId = corteId
        )
    }

    private fun agruparVentasPorTransaccion(ventas: List<VentaDiaUi>): List<GrupoVentaDiaUi> {
        return ventas
            .groupBy { it.grupoVentaId }
            .map { (grupoId, ventasDelGrupo) ->
                val ventasOrdenadas = ventasDelGrupo.sortedBy { it.horaVenta }

                GrupoVentaDiaUi(
                    grupoVentaId = grupoId,
                    ventas = ventasOrdenadas,
                    horaVenta = ventasOrdenadas.firstOrNull()?.horaVenta.orEmpty(),
                    totalCentavos = ventasOrdenadas.sumOf { it.totalCentavos },
                    piezas = ventasOrdenadas.sumOf { it.cantidad },
                    productosDistintos = ventasOrdenadas.size,
                    resumenProductos = crearResumenProductos(ventasOrdenadas),
                    tieneCorte = ventasOrdenadas.any { it.corteId != null }
                )
            }
            .sortedByDescending { it.horaVenta }
    }

    private fun crearResumenProductos(ventas: List<VentaDiaUi>): String {
        return ventas
            .take(3)
            .joinToString(separator = " · ") { venta ->
                "${venta.cantidad} ${venta.nombreProducto}"
            }
            .let { resumen ->
                if (ventas.size > 3) {
                    "$resumen · +${ventas.size - 3} más"
                } else {
                    resumen
                }
            }
    }

    private fun convertirTextoACentavos(texto: String): Long {
        if (texto.isBlank()) return 0L

        val limpio = texto
            .replace(",", ".")
            .trim()

        val valor = limpio.toDoubleOrNull() ?: return 0L

        return (valor * 100).roundToLong()
    }

    private fun centavosATexto(centavos: Long): String {
        if (centavos <= 0L) return ""
        return "%.2f".format(Locale.US, centavos / 100.0)
    }

    private fun esMontoValidoParaTexto(texto: String): Boolean {
        if (texto.isBlank()) return true

        val regex = Regex("^\\d{0,7}([.,]\\d{0,2})?$")
        return regex.matches(texto)
    }

    private fun obtenerFechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun abrirConfirmacionCancelacionProducto(venta: VentaDiaUi) {
        if (venta.corteId != null) {
            _uiState.update { estado ->
                estado.copy(
                    mensajeError = "No se puede eliminar un producto que ya pertenece a un corte.",
                    mensajeExito = null
                )
            }
            return
        }

        _uiState.update { estado ->
            estado.copy(
                ventaParaCancelar = venta,
                notaTexto = "",
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun cerrarConfirmacionCancelacionProducto() {
        _uiState.update { estado ->
            estado.copy(
                ventaParaCancelar = null,
                cancelando = false,
                notaTexto = "",
                mensajeError = null
            )
        }
    }

    fun cancelarProductoSeleccionado() {
        val estado = _uiState.value
        val venta = estado.ventaParaCancelar ?: return

        if (estado.cancelando) return

        viewModelScope.launch {
            _uiState.update { actual ->
                actual.copy(
                    cancelando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            val resultado = cancelarVentaUseCase(
                ventaId = venta.id,
                nota = estado.notaTexto.ifBlank {
                    "Producto eliminado de la transacción antes del corte."
                }
            )

            when (resultado) {
                is ResultadoCancelarVenta.Exito -> {
                    delay(700)

                    _uiState.update { actual ->
                        actual.copy(
                            ventaParaCancelar = null,
                            cancelando = false,
                            notaTexto = "",
                            mensajeExito = "Producto eliminado de la transacción.",
                            mensajeError = null
                        )
                    }
                }

                is ResultadoCancelarVenta.Error -> {
                    _uiState.update { actual ->
                        actual.copy(
                            cancelando = false,
                            mensajeError = resultado.mensaje,
                            mensajeExito = null
                        )
                    }
                }
            }
        }
    }
}
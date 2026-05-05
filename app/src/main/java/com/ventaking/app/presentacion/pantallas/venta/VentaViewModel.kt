package com.ventaking.app.presentacion.pantallas.venta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.dominio.casos.productos.ObtenerProductosPorNegocioUseCase
import com.ventaking.app.dominio.casos.ventas.RegistrarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.ResultadoRegistrarVenta
import com.ventaking.app.dominio.modelo.ProductoRapido
import com.ventaking.app.dominio.repositorio.VentaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToLong

class VentaViewModel(
    private val negocioDao: NegocioDao,
    private val obtenerProductosPorNegocioUseCase: ObtenerProductosPorNegocioUseCase,
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaUiState())
    val uiState: StateFlow<VentaUiState> = _uiState.asStateFlow()

    private var productosJob: Job? = null
    private var totalDiaJob: Job? = null

    init {
        observarNegocios()
    }

    private fun observarNegocios() {
        viewModelScope.launch {
            negocioDao.observarActivos().collect { negociosEntity ->
                val negocios = negociosEntity.map { negocio ->
                    NegocioVentaUi(
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
                    observarProductos(negocioSeleccionado)
                    observarTotalDia(negocioSeleccionado)
                }
            }
        }
    }

    fun seleccionarNegocio(id: String) {
        val negocioActual = _uiState.value.negocioSeleccionadoId
        if (id == negocioActual) return

        _uiState.update { estado ->
            estado.copy(
                negocioSeleccionadoId = id,
                productos = emptyList(),
                carrito = emptyList(),
                extraTexto = "",
                descuentoTexto = "",
                subtotalCentavos = 0L,
                totalCentavos = 0L,
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        observarProductos(id)
        observarTotalDia(id)
    }

    private fun observarProductos(negocioId: String) {
        productosJob?.cancel()

        productosJob = viewModelScope.launch {
            obtenerProductosPorNegocioUseCase(
                negocioId = negocioId,
                soloActivos = true
            ).collect { productos ->
                _uiState.update { estado ->
                    estado.copy(
                        productos = productos,
                        mensajeError = if (productos.isEmpty()) {
                            "No hay productos activos para este negocio."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    private fun observarTotalDia(negocioId: String) {
        totalDiaJob?.cancel()

        totalDiaJob = viewModelScope.launch {
            ventaRepository.observarTotalActivoPorNegocioYFecha(
                negocioId = negocioId,
                fechaVenta = obtenerFechaActual()
            ).collect { total ->
                _uiState.update { estado ->
                    estado.copy(totalDiaCentavos = total ?: 0L)
                }
            }
        }
    }

    fun agregarAlCarrito(producto: ProductoRapido) {
        if (!producto.estaActivo) {
            _uiState.update { estado ->
                estado.copy(
                    mensajeError = "Este producto está inactivo.",
                    mensajeExito = null
                )
            }
            return
        }

        _uiState.update { estado ->
            val carritoActualizado = estado.carrito.toMutableList()
            val index = carritoActualizado.indexOfFirst { it.producto.id == producto.id }

            if (index >= 0) {
                val itemActual = carritoActualizado[index]
                carritoActualizado[index] = itemActual.copy(
                    cantidad = itemActual.cantidad + 1
                )
            } else {
                carritoActualizado.add(
                    CartItem(
                        producto = producto,
                        cantidad = 1
                    )
                )
            }

            estado.copy(
                carrito = carritoActualizado,
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        recalcularTotales()
    }

    fun modificarCantidad(productoId: String, delta: Int) {
        _uiState.update { estado ->
            val carritoActualizado = estado.carrito.mapNotNull { item ->
                if (item.producto.id == productoId) {
                    val nuevaCantidad = item.cantidad + delta

                    if (nuevaCantidad > 0) {
                        item.copy(cantidad = nuevaCantidad)
                    } else {
                        null
                    }
                } else {
                    item
                }
            }

            estado.copy(
                carrito = carritoActualizado,
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        recalcularTotales()
    }

    fun eliminarDelCarrito(productoId: String) {
        _uiState.update { estado ->
            estado.copy(
                carrito = estado.carrito.filterNot { it.producto.id == productoId },
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        recalcularTotales()
    }

    fun cambiarExtra(texto: String) {
        if (!esMontoValidoParaTexto(texto)) return

        _uiState.update { estado ->
            estado.copy(
                extraTexto = texto,
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        recalcularTotales()
    }

    fun cambiarDescuento(texto: String) {
        if (!esMontoValidoParaTexto(texto)) return

        _uiState.update { estado ->
            estado.copy(
                descuentoTexto = texto,
                mensajeError = null,
                mensajeExito = null,
                guardando = false
            )
        }

        recalcularTotales()
    }

    private fun recalcularTotales() {
        val estado = _uiState.value

        val subtotal = estado.carrito.sumOf { it.totalItemCentavos }
        val extra = convertirTextoACentavos(estado.extraTexto)
        val descuento = convertirTextoACentavos(estado.descuentoTexto)
        val total = subtotal + extra - descuento

        val error = when {
            estado.carrito.isEmpty() -> null
            descuento >= subtotal + extra -> "El descuento no puede ser igual o mayor al subtotal más extra."
            total <= 0L -> "El total debe ser mayor a cero."
            else -> null
        }

        _uiState.update { estadoActual ->
            estadoActual.copy(
                subtotalCentavos = subtotal,
                totalCentavos = if (total > 0L) total else 0L,
                mensajeError = error
            )
        }
    }

    fun registrarVenta() {
        val estado = _uiState.value
        val negocioId = estado.negocioSeleccionadoId

        if (estado.guardando) return

        if (negocioId.isNullOrBlank()) {
            _uiState.update { actual ->
                actual.copy(
                    mensajeError = "Selecciona un negocio antes de vender.",
                    mensajeExito = null
                )
            }
            return
        }

        if (estado.carrito.isEmpty()) {
            _uiState.update { actual ->
                actual.copy(
                    mensajeError = "Agrega al menos un producto al carrito.",
                    mensajeExito = null
                )
            }
            return
        }

        val subtotal = estado.carrito.sumOf { it.totalItemCentavos }
        val extra = convertirTextoACentavos(estado.extraTexto)
        val descuento = convertirTextoACentavos(estado.descuentoTexto)
        val totalVenta = subtotal + extra - descuento

        if (descuento >= subtotal + extra) {
            _uiState.update { actual ->
                actual.copy(
                    mensajeError = "El descuento no puede ser igual o mayor al subtotal más extra.",
                    mensajeExito = null
                )
            }
            return
        }

        if (totalVenta <= 0L) {
            _uiState.update { actual ->
                actual.copy(
                    mensajeError = "El total debe ser mayor a cero.",
                    mensajeExito = null
                )
            }
            return
        }

        viewModelScope.launch {
            val inicioGuardado = System.currentTimeMillis()
            val grupoVentaId = UUID.randomUUID().toString()
            val carritoActual = estado.carrito

            _uiState.update { actual ->
                actual.copy(
                    guardando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            val extrasDistribuidos = distribuirMontoProporcional(
                totalADistribuir = extra,
                items = carritoActual
            )

            val descuentosDistribuidos = distribuirMontoProporcional(
                totalADistribuir = descuento,
                items = carritoActual
            )

            var ventasGuardadas = 0
            var error: String? = null

            for ((index, item) in carritoActual.withIndex()) {
                val resultado = registrarVentaUseCase(
                    grupoVentaId = grupoVentaId,
                    negocioId = negocioId,
                    producto = item.producto,
                    cantidad = item.cantidad,
                    extraCentavos = extrasDistribuidos[index],
                    descuentoCentavos = descuentosDistribuidos[index]
                )

                when (resultado) {
                    is ResultadoRegistrarVenta.Exito -> ventasGuardadas++
                    is ResultadoRegistrarVenta.Error -> {
                        error = resultado.mensaje
                        break
                    }
                }
            }

            val tiempoTranscurrido = System.currentTimeMillis() - inicioGuardado
            val tiempoRestanteAnimacion = 900L - tiempoTranscurrido

            if (tiempoRestanteAnimacion > 0L) {
                delay(tiempoRestanteAnimacion)
            }

            if (error == null) {
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        carrito = emptyList(),
                        extraTexto = "",
                        descuentoTexto = "",
                        subtotalCentavos = 0L,
                        totalCentavos = totalVenta,
                        mensajeExito = if (ventasGuardadas == 1) {
                            "Venta registrada correctamente."
                        } else {
                            "Venta registrada correctamente con $ventasGuardadas productos."
                        },
                        mensajeError = null
                    )
                }
            } else {
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        mensajeError = error,
                        mensajeExito = null
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { estado ->
            estado.copy(
                mensajeExito = null,
                mensajeError = null,
                subtotalCentavos = if (estado.carrito.isEmpty()) 0L else estado.subtotalCentavos,
                totalCentavos = if (estado.carrito.isEmpty()) 0L else estado.totalCentavos,
                guardando = false
            )
        }
    }

    private fun distribuirMontoProporcional(
        totalADistribuir: Long,
        items: List<CartItem>
    ): List<Long> {
        if (totalADistribuir <= 0L || items.isEmpty()) {
            return List(items.size) { 0L }
        }

        val subtotal = items.sumOf { it.totalItemCentavos }

        if (subtotal <= 0L) {
            return List(items.size) { 0L }
        }

        val distribucion = MutableList(items.size) { 0L }
        var acumulado = 0L

        items.forEachIndexed { index, item ->
            val monto = if (index == items.lastIndex) {
                totalADistribuir - acumulado
            } else {
                ((item.totalItemCentavos.toDouble() / subtotal.toDouble()) * totalADistribuir)
                    .roundToLong()
            }

            distribucion[index] = monto
            acumulado += monto
        }

        return distribucion
    }

    private fun convertirTextoACentavos(texto: String): Long {
        if (texto.isBlank()) return 0L

        val limpio = texto
            .replace(",", ".")
            .trim()

        val valor = limpio.toDoubleOrNull() ?: return 0L

        return (valor * 100).roundToLong()
    }

    private fun esMontoValidoParaTexto(texto: String): Boolean {
        if (texto.isBlank()) return true

        val regex = Regex("^\\d{0,7}([.,]\\d{0,2})?$")
        return regex.matches(texto)
    }

    private fun obtenerFechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun formatearCentavos(centavos: Long): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(centavos / 100.0)
    }
}
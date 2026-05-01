package com.ventaking.app.presentacion.pantallas.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.dominio.casos.productos.CrearProductoUseCase
import com.ventaking.app.dominio.casos.productos.DesactivarProductoUseCase
import com.ventaking.app.dominio.casos.productos.EditarProductoUseCase
import com.ventaking.app.dominio.casos.productos.ObtenerProductosPorNegocioUseCase
import com.ventaking.app.dominio.casos.productos.ReactivarProductoUseCase
import com.ventaking.app.dominio.modelo.ProductoRapido
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToLong

class ProductosViewModel(
    private val negocioDao: NegocioDao,
    private val obtenerProductosPorNegocioUseCase: ObtenerProductosPorNegocioUseCase,
    private val crearProductoUseCase: CrearProductoUseCase,
    private val editarProductoUseCase: EditarProductoUseCase,
    private val desactivarProductoUseCase: DesactivarProductoUseCase,
    private val reactivarProductoUseCase: ReactivarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    private var productosJob: Job? = null

    init {
        cargarNegocios()
    }

    private fun cargarNegocios() {
        viewModelScope.launch {
            negocioDao.observarActivos().collect { lista ->
                val negocios = lista.map {
                    NegocioProductoUi(
                        id = it.id,
                        nombre = it.nombre
                    )
                }

                _uiState.update {
                    it.copy(negocios = negocios)
                }

                if (_uiState.value.negocioIdSeleccionado.isBlank() && negocios.isNotEmpty()) {
                    seleccionarNegocio(negocios.first().id)
                }
            }
        }
    }

    fun seleccionarNegocio(negocioId: String) {
        val negocio = _uiState.value.negocios.firstOrNull { it.id == negocioId }

        _uiState.update {
            it.copy(
                negocioIdSeleccionado = negocioId,
                negocioNombreSeleccionado = negocio?.nombre.orEmpty(),
                formularioVisible = false,
                productoEditando = null,
                productoParaConfirmar = null,
                mensaje = null,
                error = null,
                estaCargando = true
            )
        }

        observarProductos(negocioId)
    }

    private fun observarProductos(negocioId: String) {
        productosJob?.cancel()

        productosJob = viewModelScope.launch {
            obtenerProductosPorNegocioUseCase(
                negocioId = negocioId,
                soloActivos = false
            ).collect { productos ->
                _uiState.update {
                    it.copy(
                        productos = productos,
                        estaCargando = false
                    )
                }
            }
        }
    }

    fun cambiarFiltro(filtro: FiltroProducto) {
        _uiState.update {
            it.copy(
                filtro = filtro,
                mensaje = null,
                error = null
            )
        }
    }

    fun abrirNuevoProducto() {
        _uiState.update {
            it.copy(
                formularioVisible = true,
                productoEditando = null,
                nombre = "",
                precio = "",
                categoria = "",
                esPromocion = false,
                mensaje = null,
                error = null
            )
        }
    }

    fun abrirEdicion(producto: ProductoRapido) {
        if (!producto.estaActivo) {
            _uiState.update {
                it.copy(
                    error = "Primero reactiva el producto para poder editarlo.",
                    mensaje = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                formularioVisible = true,
                productoEditando = producto,
                nombre = producto.nombre,
                precio = centavosATexto(producto.precioCentavos),
                categoria = producto.categoria.orEmpty(),
                esPromocion = producto.esPromocion,
                mensaje = null,
                error = null
            )
        }
    }

    fun cerrarFormulario() {
        _uiState.update {
            it.copy(
                formularioVisible = false,
                productoEditando = null,
                nombre = "",
                precio = "",
                categoria = "",
                esPromocion = false
            )
        }
    }

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(nombre = valor, mensaje = null, error = null)
        }
    }

    fun cambiarPrecio(valor: String) {
        _uiState.update {
            it.copy(precio = limpiarPrecio(valor), mensaje = null, error = null)
        }
    }

    fun cambiarCategoria(valor: String) {
        _uiState.update {
            it.copy(categoria = valor, mensaje = null, error = null)
        }
    }

    fun cambiarEsPromocion(valor: Boolean) {
        _uiState.update {
            it.copy(esPromocion = valor, mensaje = null, error = null)
        }
    }

    fun guardarProducto() {
        val estado = _uiState.value
        val precioCentavos = textoACentavos(estado.precio)

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(estaCargando = true, error = null, mensaje = null) }

                val productoEditando = estado.productoEditando

                if (productoEditando == null) {
                    crearProductoUseCase(
                        negocioId = estado.negocioIdSeleccionado,
                        nombre = estado.nombre,
                        precioCentavos = precioCentavos,
                        categoria = estado.categoria,
                        esPromocion = estado.esPromocion
                    )

                    _uiState.update {
                        it.copy(
                            formularioVisible = false,
                            estaCargando = false,
                            nombre = "",
                            precio = "",
                            categoria = "",
                            esPromocion = false,
                            mensaje = "Producto creado."
                        )
                    }
                } else {
                    editarProductoUseCase(
                        productoId = productoEditando.id,
                        nombre = estado.nombre,
                        precioCentavos = precioCentavos,
                        categoria = estado.categoria,
                        esPromocion = estado.esPromocion
                    )

                    _uiState.update {
                        it.copy(
                            formularioVisible = false,
                            productoEditando = null,
                            estaCargando = false,
                            nombre = "",
                            precio = "",
                            categoria = "",
                            esPromocion = false,
                            mensaje = "Producto actualizado."
                        )
                    }
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        estaCargando = false,
                        error = e.message ?: "Revisa los datos."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        estaCargando = false,
                        error = "No se pudo guardar el producto."
                    )
                }
            }
        }
    }

    fun pedirConfirmacionCambioEstado(producto: ProductoRapido) {
        _uiState.update {
            it.copy(        
                productoParaConfirmar = producto,
                mensaje = null,
                error = null
            )
        }
    }

    fun cancelarConfirmacion() {
        _uiState.update {
            it.copy(productoParaConfirmar = null)
        }
    }

    fun confirmarCambioEstado() {
        val producto = _uiState.value.productoParaConfirmar ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(estaCargando = true) }

                if (producto.estaActivo) {
                    desactivarProductoUseCase(producto.id)

                    _uiState.update {
                        it.copy(
                            productoParaConfirmar = null,
                            productoEditando = null,
                            formularioVisible = false,
                            estaCargando = false,
                            filtro = FiltroProducto.ACTIVOS,
                            mensaje = "Producto desactivado."
                        )
                    }
                } else {
                    reactivarProductoUseCase(producto.id)

                    _uiState.update {
                        it.copy(
                            productoParaConfirmar = null,
                            estaCargando = false,
                            filtro = FiltroProducto.ACTIVOS,
                            mensaje = "Producto reactivado."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        productoParaConfirmar = null,
                        estaCargando = false,
                        error = "No se pudo cambiar el estado del producto."
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(mensaje = null, error = null)
        }
    }

    private fun textoACentavos(texto: String): Long {
        val valor = texto.toDoubleOrNull() ?: 0.0
        return (valor * 100).roundToLong()
    }

    private fun centavosATexto(centavos: Long): String {
        return String.format(Locale.US, "%.2f", centavos / 100.0)
    }

    private fun limpiarPrecio(valor: String): String {
        var puntoUsado = false
        val limpio = valor.replace(",", ".").filter { caracter ->
            when {
                caracter.isDigit() -> true
                caracter == '.' && !puntoUsado -> {
                    puntoUsado = true
                    true
                }
                else -> false
            }
        }

        val partes = limpio.split(".")
        return if (partes.size == 2) {
            partes[0] + "." + partes[1].take(2)
        } else {
            limpio
        }
    }
}
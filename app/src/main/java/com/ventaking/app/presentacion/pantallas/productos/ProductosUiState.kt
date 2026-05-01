package com.ventaking.app.presentacion.pantallas.productos

import com.ventaking.app.dominio.modelo.ProductoRapido

data class ProductosUiState(
    val negocios: List<NegocioProductoUi> = emptyList(),
    val negocioIdSeleccionado: String = "",
    val negocioNombreSeleccionado: String = "",

    val productos: List<ProductoRapido> = emptyList(),
    val filtro: FiltroProducto = FiltroProducto.ACTIVOS,

    val formularioVisible: Boolean = false,
    val productoEditando: ProductoRapido? = null,

    val nombre: String = "",
    val precio: String = "",
    val categoria: String = "",
    val esPromocion: Boolean = false,

    val productoParaConfirmar: ProductoRapido? = null,

    val estaCargando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
) {
    val activos: List<ProductoRapido>
        get() = productos.filter { it.estaActivo }

    val inactivos: List<ProductoRapido>
        get() = productos.filter { !it.estaActivo }

    val productosVisibles: List<ProductoRapido>
        get() = when (filtro) {
            FiltroProducto.ACTIVOS -> activos
            FiltroProducto.INACTIVOS -> inactivos
            FiltroProducto.TODOS -> productos
        }

    val puedeGuardar: Boolean
        get() = negocioIdSeleccionado.isNotBlank() &&
                nombre.trim().isNotBlank() &&
                precio.trim().isNotBlank() &&
                !estaCargando

    val estaEditando: Boolean
        get() = productoEditando != null
}

data class NegocioProductoUi(
    val id: String,
    val nombre: String
)

enum class FiltroProducto {
    ACTIVOS,
    INACTIVOS,
    TODOS
}
package com.ventaking.app.presentacion.navegacion

sealed class Rutas(val ruta: String) {
    data object Inicio : Rutas("inicio")
    data object Venta : Rutas("venta")
    data object Productos : Rutas("productos")
    data object VentasDia : Rutas("ventas_dia")
    data object Corte : Rutas("corte")
    data object Historial : Rutas("historial")
    data object Sincronizacion : Rutas("sincronizacion")
    data object Configuracion : Rutas("configuracion")
}
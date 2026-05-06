package com.ventaking.app.presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ventaking.app.presentacion.pantallas.configuracion.ConfiguracionScreen
import com.ventaking.app.presentacion.pantallas.configuracion.ConfiguracionViewModel
import com.ventaking.app.presentacion.pantallas.corte.CorteScreen
import com.ventaking.app.presentacion.pantallas.historial.HistorialScreen
import com.ventaking.app.presentacion.pantallas.inicio.InicioScreen
import com.ventaking.app.presentacion.pantallas.productos.ProductosScreen
import com.ventaking.app.presentacion.pantallas.productos.ProductosViewModel
import com.ventaking.app.presentacion.pantallas.sincronizacion.SincronizacionScreen
import com.ventaking.app.presentacion.pantallas.venta.VentaScreen
import com.ventaking.app.presentacion.pantallas.venta.VentaViewModel
import com.ventaking.app.presentacion.pantallas.ventas_dia.VentasDiaScreen
import com.ventaking.app.presentacion.pantallas.ventas_dia.VentasDiaViewModel
import com.ventaking.app.presentacion.pantallas.corte.CorteViewModel

@Composable
fun AppNavigation(
    configuracionViewModel: ConfiguracionViewModel,
    productosViewModel: ProductosViewModel,
    ventaViewModel: VentaViewModel,
    ventasDiaViewModel: VentasDiaViewModel,
    corteViewModel: CorteViewModel

) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.Inicio.ruta
    ) {
        composable(Rutas.Inicio.ruta) {
            InicioScreen(
                onIrAVenta = {
                    navController.navigate(Rutas.Venta.ruta)
                },
                onIrAProductos = {
                    navController.navigate(Rutas.Productos.ruta)
                },
                onIrAVentasDia = {
                    navController.navigate(Rutas.VentasDia.ruta)
                },
                onIrACorte = {
                    navController.navigate(Rutas.Corte.ruta)
                },
                onIrAHistorial = {
                    navController.navigate(Rutas.Historial.ruta)
                },
                onIrASincronizacion = {
                    navController.navigate(Rutas.Sincronizacion.ruta)
                },
                onIrAConfiguracion = {
                    navController.navigate(Rutas.Configuracion.ruta)
                }
            )
        }

        composable(Rutas.Venta.ruta) {
            VentaScreen(
                viewModel = ventaViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Productos.ruta) {
            ProductosScreen(
                viewModel = productosViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.VentasDia.ruta) {
            VentasDiaScreen(
                viewModel = ventasDiaViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Corte.ruta) {
            CorteScreen(
                viewModel = corteViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Historial.ruta) {
            HistorialScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Sincronizacion.ruta) {
            SincronizacionScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Configuracion.ruta) {
            ConfiguracionScreen(
                viewModel = configuracionViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}
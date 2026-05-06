package com.ventaking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.repositorio.ConfiguracionAppRepositoryImpl
import com.ventaking.app.datos.repositorio.DispositivoRepositoryImpl
import com.ventaking.app.datos.repositorio.NegocioRepositoryImpl
import com.ventaking.app.datos.repositorio.ProductoRapidoRepositoryImpl
import com.ventaking.app.datos.repositorio.VentaRepositoryImpl
import com.ventaking.app.dominio.casos.InicializarAppUseCase
import com.ventaking.app.dominio.casos.productos.CrearProductoUseCase
import com.ventaking.app.dominio.casos.productos.DesactivarProductoUseCase
import com.ventaking.app.dominio.casos.productos.EditarProductoUseCase
import com.ventaking.app.dominio.casos.productos.ObtenerProductosPorNegocioUseCase
import com.ventaking.app.dominio.casos.productos.ReactivarProductoUseCase
import com.ventaking.app.dominio.casos.ventas.CalcularTotalVentaUseCase
import com.ventaking.app.dominio.casos.ventas.CancelarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.EditarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.ObtenerVentasDelDiaUseCase
import com.ventaking.app.dominio.casos.ventas.RegistrarHistorialVentaUseCase
import com.ventaking.app.dominio.casos.ventas.RegistrarVentaUseCase
import com.ventaking.app.presentacion.navegacion.AppNavigation
import com.ventaking.app.presentacion.pantallas.configuracion.ConfiguracionViewModel
import com.ventaking.app.presentacion.pantallas.productos.ProductosViewModel
import com.ventaking.app.presentacion.pantallas.venta.VentaViewModel
import com.ventaking.app.presentacion.pantallas.ventas_dia.VentasDiaViewModel
import com.ventaking.app.presentacion.tema.TemaVentaKing
import kotlinx.coroutines.launch
import com.ventaking.app.datos.repositorio.CorteRepositoryImpl
import com.ventaking.app.dominio.casos.corte.CrearCorteDiarioUseCase
import com.ventaking.app.dominio.casos.corte.ObtenerResumenCorteUseCase
import com.ventaking.app.presentacion.pantallas.corte.CorteViewModel

class MainActivity : ComponentActivity() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ventaking_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val negocioRepository: NegocioRepositoryImpl by lazy {
        NegocioRepositoryImpl(
            negocioDao = database.negocioDao()
        )
    }

    private val dispositivoRepository: DispositivoRepositoryImpl by lazy {
        DispositivoRepositoryImpl(
            dispositivoDao = database.dispositivoDao()
        )
    }

    private val configuracionAppRepository: ConfiguracionAppRepositoryImpl by lazy {
        ConfiguracionAppRepositoryImpl(
            configuracionAppDao = database.configuracionAppDao()
        )
    }

    private val productoRapidoRepository: ProductoRapidoRepositoryImpl by lazy {
        ProductoRapidoRepositoryImpl(
            productoRapidoDao = database.productoRapidoDao()
        )
    }

    private val ventaRepository: VentaRepositoryImpl by lazy {
        VentaRepositoryImpl(
            ventaDao = database.ventaDao(),
            historialVentaDao = database.historialVentaDao()
        )
    }

    private val corteRepository: CorteRepositoryImpl by lazy {
        CorteRepositoryImpl(
            database = database,
            corteDiarioDao = database.corteDiarioDao(),
            ventaDao = database.ventaDao(),
            historialVentaDao = database.historialVentaDao()
        )
    }

    private val obtenerProductosPorNegocioUseCase: ObtenerProductosPorNegocioUseCase by lazy {
        ObtenerProductosPorNegocioUseCase(
            productoRapidoRepository = productoRapidoRepository
        )
    }

    private val calcularTotalVentaUseCase: CalcularTotalVentaUseCase by lazy {
        CalcularTotalVentaUseCase()
    }

    private val registrarHistorialVentaUseCase: RegistrarHistorialVentaUseCase by lazy {
        RegistrarHistorialVentaUseCase(
            ventaRepository = ventaRepository
        )
    }

    private val registrarVentaUseCase: RegistrarVentaUseCase by lazy {
        RegistrarVentaUseCase(
            ventaRepository = ventaRepository,
            dispositivoRepository = dispositivoRepository,
            calcularTotalVentaUseCase = calcularTotalVentaUseCase,
            registrarHistorialVentaUseCase = registrarHistorialVentaUseCase
        )
    }

    private val obtenerVentasDelDiaUseCase: ObtenerVentasDelDiaUseCase by lazy {
        ObtenerVentasDelDiaUseCase(
            ventaRepository = ventaRepository
        )
    }

    private val editarVentaUseCase: EditarVentaUseCase by lazy {
        EditarVentaUseCase(
            ventaRepository = ventaRepository,
            calcularTotalVentaUseCase = calcularTotalVentaUseCase
        )
    }

    private val cancelarVentaUseCase: CancelarVentaUseCase by lazy {
        CancelarVentaUseCase(
            ventaRepository = ventaRepository
        )
    }

    private val obtenerResumenCorteUseCase: ObtenerResumenCorteUseCase by lazy {
        ObtenerResumenCorteUseCase(
            corteRepository = corteRepository
        )
    }

    private val crearCorteDiarioUseCase: CrearCorteDiarioUseCase by lazy {
        CrearCorteDiarioUseCase(
            corteRepository = corteRepository
        )
    }

    private val inicializarAppUseCase: InicializarAppUseCase by lazy {
        InicializarAppUseCase(
            negocioRepository = negocioRepository,
            dispositivoRepository = dispositivoRepository,
            configuracionAppRepository = configuracionAppRepository
        )
    }

    private val configuracionViewModel: ConfiguracionViewModel by lazy {
        ConfiguracionViewModel(
            dispositivoRepository = dispositivoRepository,
            configuracionAppRepository = configuracionAppRepository
        )
    }

    private val productosViewModel: ProductosViewModel by lazy {
        ProductosViewModel(
            negocioDao = database.negocioDao(),
            obtenerProductosPorNegocioUseCase = obtenerProductosPorNegocioUseCase,
            crearProductoUseCase = CrearProductoUseCase(
                productoRapidoRepository = productoRapidoRepository
            ),
            editarProductoUseCase = EditarProductoUseCase(
                productoRapidoRepository = productoRapidoRepository
            ),
            desactivarProductoUseCase = DesactivarProductoUseCase(
                productoRapidoRepository = productoRapidoRepository
            ),
            reactivarProductoUseCase = ReactivarProductoUseCase(
                productoRapidoRepository = productoRapidoRepository
            )
        )
    }

    private val ventaViewModel: VentaViewModel by lazy {
        VentaViewModel(
            negocioDao = database.negocioDao(),
            corteDiarioDao = database.corteDiarioDao(),
            dispositivoRepository = dispositivoRepository,
            obtenerProductosPorNegocioUseCase = obtenerProductosPorNegocioUseCase,
            registrarVentaUseCase = registrarVentaUseCase,
            ventaRepository = ventaRepository
        )
    }


    private val ventasDiaViewModel: VentasDiaViewModel by lazy {
        VentasDiaViewModel(
            negocioDao = database.negocioDao(),
            obtenerVentasDelDiaUseCase = obtenerVentasDelDiaUseCase,
            editarVentaUseCase = editarVentaUseCase,
            cancelarVentaUseCase = cancelarVentaUseCase
        )
    }

    private val corteViewModel: CorteViewModel by lazy {
        CorteViewModel(
            negocioDao = database.negocioDao(),
            dispositivoRepository = dispositivoRepository,
            obtenerResumenCorteUseCase = obtenerResumenCorteUseCase,
            crearCorteDiarioUseCase = crearCorteDiarioUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inicializarDatosLocales()

        setContent {
            TemaVentaKing {
                AppNavigation(
                    configuracionViewModel = configuracionViewModel,
                    productosViewModel = productosViewModel,
                    ventaViewModel = ventaViewModel,
                    ventasDiaViewModel = ventasDiaViewModel,
                    corteViewModel = corteViewModel

                )
            }
        }
    }

    private fun inicializarDatosLocales() {
        lifecycleScope.launch {
            inicializarAppUseCase()
            configuracionViewModel.cargarConfiguracion()
        }
    }
}
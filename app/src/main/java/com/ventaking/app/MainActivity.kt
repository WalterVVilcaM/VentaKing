package com.ventaking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.ventaking.app.datos.excel.GeneradorExcel
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.repositorio.ConfiguracionAppRepositoryImpl
import com.ventaking.app.datos.repositorio.CorteRepositoryImpl
import com.ventaking.app.datos.repositorio.DispositivoRepositoryImpl
import com.ventaking.app.datos.repositorio.ExportacionRepositoryImpl
import com.ventaking.app.datos.repositorio.NegocioRepositoryImpl
import com.ventaking.app.datos.repositorio.ProductoRapidoRepositoryImpl
import com.ventaking.app.datos.repositorio.VentaRepositoryImpl
import com.ventaking.app.datos.respaldo.GeneradorJson
import com.ventaking.app.dominio.casos.InicializarAppUseCase
import com.ventaking.app.dominio.casos.corte.CrearCorteDiarioUseCase
import com.ventaking.app.dominio.casos.corte.ObtenerResumenCorteUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteExcelUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteJsonUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarVentasJsonUseCase
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
import com.ventaking.app.presentacion.pantallas.corte.CorteViewModel
import com.ventaking.app.presentacion.pantallas.productos.ProductosViewModel
import com.ventaking.app.presentacion.pantallas.venta.VentaViewModel
import com.ventaking.app.presentacion.pantallas.ventas_dia.VentasDiaViewModel
import com.ventaking.app.presentacion.tema.TemaVentaKing
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.ventaking.app.datos.drive.DriveServiceProvider
import com.ventaking.app.datos.repositorio.SincronizacionRepositoryImpl
import com.ventaking.app.dominio.casos.sincronizacion.SubirArchivosCorteUseCase
import com.ventaking.app.nucleo.constantes.GoogleDriveConfig
import java.util.Collections

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val driveServiceProvider: DriveServiceProvider by lazy {
        DriveServiceProvider()
    }

    private val driveSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)

            try {
                val cuenta = tarea.getResult(ApiException::class.java)
                configurarDriveConCuenta(cuenta)
                corteViewModel.respaldarCorteActualEnDrive()
            } catch (e: ApiException) {
                driveServiceProvider.cerrarConexion()
            }
        }

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

    private val generadorJson: GeneradorJson by lazy {
        GeneradorJson()
    }

    private val generadorExcel: GeneradorExcel by lazy {
        GeneradorExcel()
    }

    private val exportacionRepository: ExportacionRepositoryImpl by lazy {
        ExportacionRepositoryImpl(
            context = applicationContext,
            corteDiarioDao = database.corteDiarioDao(),
            ventaDao = database.ventaDao(),
            negocioDao = database.negocioDao(),
            dispositivoDao = database.dispositivoDao(),
            registroArchivoSyncDao = database.registroArchivoSyncDao(),
            generadorJson = generadorJson,
            generadorExcel = generadorExcel
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

    private val exportarVentasJsonUseCase: ExportarVentasJsonUseCase by lazy {
        ExportarVentasJsonUseCase(
            exportacionRepository = exportacionRepository
        )
    }

    private val exportarCorteJsonUseCase: ExportarCorteJsonUseCase by lazy {
        ExportarCorteJsonUseCase(
            exportacionRepository = exportacionRepository
        )
    }

    private val exportarCorteExcelUseCase: ExportarCorteExcelUseCase by lazy {
        ExportarCorteExcelUseCase(
            exportacionRepository = exportacionRepository
        )
    }

    private val sincronizacionRepository: SincronizacionRepositoryImpl by lazy {
        SincronizacionRepositoryImpl(
            registroArchivoSyncDao = database.registroArchivoSyncDao(),
            corteDiarioDao = database.corteDiarioDao(),
            dispositivoDao = database.dispositivoDao(),
            driveServiceProvider = driveServiceProvider
        )
    }

    private val subirArchivosCorteUseCase: SubirArchivosCorteUseCase by lazy {
        SubirArchivosCorteUseCase(
            sincronizacionRepository = sincronizacionRepository
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
            crearCorteDiarioUseCase = crearCorteDiarioUseCase,
            exportarVentasJsonUseCase = exportarVentasJsonUseCase,
            exportarCorteJsonUseCase = exportarCorteJsonUseCase,
            exportarCorteExcelUseCase = exportarCorteExcelUseCase,
            subirArchivosCorteUseCase = subirArchivosCorteUseCase,
            driveConectado = {
                driveServiceProvider.estaConectado()
            },
            solicitarConexionDrive = {
                solicitarConexionDrive()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configurarGoogleDrive()
        configurarDriveSiYaAutorizado()
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

    private fun configurarGoogleDrive() {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, opciones)
    }

    private fun configurarDriveSiYaAutorizado() {
        val cuenta = GoogleSignIn.getLastSignedInAccount(this)

        if (cuenta != null && GoogleSignIn.hasPermissions(cuenta, Scope(DriveScopes.DRIVE_FILE))) {
            configurarDriveConCuenta(cuenta)
        }
    }

    private fun solicitarConexionDrive() {
        driveSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun configurarDriveConCuenta(cuenta: GoogleSignInAccount) {
        val credencial = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )

        credencial.selectedAccount = cuenta.account

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credencial
        )
            .setApplicationName(GoogleDriveConfig.APP_NAME)
            .build()

        driveServiceProvider.configurar(driveService)
    }

    private fun inicializarDatosLocales() {
        lifecycleScope.launch {
            inicializarAppUseCase()
            configuracionViewModel.cargarConfiguracion()
        }
    }
}
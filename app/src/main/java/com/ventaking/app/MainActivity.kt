package com.ventaking.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.ventaking.app.datos.drive.DriveServiceProvider
import com.ventaking.app.datos.excel.GeneradorExcel
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.repositorio.ConfiguracionAppRepositoryImpl
import com.ventaking.app.datos.repositorio.CorteRepositoryImpl
import com.ventaking.app.datos.repositorio.DispositivoRepositoryImpl
import com.ventaking.app.datos.repositorio.ExportacionRepositoryImpl
import com.ventaking.app.datos.repositorio.HistorialRepositoryImpl
import com.ventaking.app.datos.repositorio.ImportacionRepositoryImpl
import com.ventaking.app.datos.repositorio.NegocioRepositoryImpl
import com.ventaking.app.datos.repositorio.ProductoRapidoRepositoryImpl
import com.ventaking.app.datos.repositorio.SincronizacionRepositoryImpl
import com.ventaking.app.datos.repositorio.VentaRepositoryImpl
import com.ventaking.app.datos.respaldo.GeneradorJson
import com.ventaking.app.dominio.casos.InicializarAppUseCase
import com.ventaking.app.dominio.casos.corte.CrearCorteDiarioUseCase
import com.ventaking.app.dominio.casos.corte.ObtenerResumenCorteUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteExcelUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteJsonUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarVentasJsonUseCase
import com.ventaking.app.dominio.casos.historial.ImportarVentasJsonUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerCortesHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerDispositivoActualHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerHistorialGrupoVentaUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerMovimientosHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerNegociosHistorialUseCase
import com.ventaking.app.dominio.casos.historial.ObtenerVentasHistorialUseCase
import com.ventaking.app.dominio.casos.productos.CrearProductoUseCase
import com.ventaking.app.dominio.casos.productos.DesactivarProductoUseCase
import com.ventaking.app.dominio.casos.productos.EditarProductoUseCase
import com.ventaking.app.dominio.casos.productos.ObtenerProductosPorNegocioUseCase
import com.ventaking.app.dominio.casos.productos.ReactivarProductoUseCase
import com.ventaking.app.dominio.casos.sincronizacion.ReintentarSincronizacionUseCase
import com.ventaking.app.dominio.casos.sincronizacion.SubirArchivosCorteUseCase
import com.ventaking.app.dominio.casos.sincronizacion.VerificarConexionUseCase
import com.ventaking.app.dominio.casos.ventas.CalcularTotalVentaUseCase
import com.ventaking.app.dominio.casos.ventas.CancelarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.EditarVentaUseCase
import com.ventaking.app.dominio.casos.ventas.ObtenerVentasDelDiaUseCase
import com.ventaking.app.dominio.casos.ventas.RegistrarHistorialVentaUseCase
import com.ventaking.app.dominio.casos.ventas.RegistrarVentaUseCase
import com.ventaking.app.dominio.repositorio.ResultadoExportacion
import com.ventaking.app.nucleo.constantes.GoogleDriveConfig
import com.ventaking.app.nucleo.red.MonitorConexion
import com.ventaking.app.presentacion.navegacion.AppNavigation
import com.ventaking.app.presentacion.pantallas.configuracion.ConfiguracionViewModel
import com.ventaking.app.presentacion.pantallas.corte.CorteViewModel
import com.ventaking.app.presentacion.pantallas.historial.HistorialViewModel
import com.ventaking.app.presentacion.pantallas.productos.ProductosViewModel
import com.ventaking.app.presentacion.pantallas.sincronizacion.SincronizacionViewModel
import com.ventaking.app.presentacion.pantallas.venta.VentaViewModel
import com.ventaking.app.presentacion.pantallas.ventas_dia.VentasDiaViewModel
import com.ventaking.app.presentacion.tema.TemaVentaKing
import java.io.File
import java.util.Collections
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private var cortePendienteExportarId: String? = null

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
            } catch (_: ApiException) {
                driveServiceProvider.cerrarConexion()
            }
        }

    private val importarJsonLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult

            lifecycleScope.launch {
                try {
                    val contenido = contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }

                    if (contenido.isNullOrBlank()) {
                        historialViewModel.mostrarMensajeAccion(
                            "No se pudo leer el archivo seleccionado."
                        )
                        return@launch
                    }

                    historialViewModel.importarVentasJsonDesdeContenido(contenido)
                } catch (e: Exception) {
                    historialViewModel.mostrarMensajeAccion(
                        e.message ?: "No se pudo importar el archivo."
                    )
                }
            }
        }

    private val exportarJsonLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri == null) return@registerForActivityResult

            val corteId = cortePendienteExportarId

            if (corteId.isNullOrBlank()) {
                historialViewModel.mostrarMensajeAccion(
                    "Selecciona un corte antes de exportar."
                )
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                exportarCorteHistorialAUri(
                    corteId = corteId,
                    uriDestino = uri
                )
            }
        }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ventaking_db"
        )
//            .fallbackToDestructiveMigration()
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

    private val historialRepository: HistorialRepositoryImpl by lazy {
        HistorialRepositoryImpl(
            ventaDao = database.ventaDao(),
            corteDiarioDao = database.corteDiarioDao(),
            historialVentaDao = database.historialVentaDao()
        )
    }

    private val importacionRepository: ImportacionRepositoryImpl by lazy {
        ImportacionRepositoryImpl(
            database = database,
            negocioDao = database.negocioDao(),
            ventaDao = database.ventaDao(),
            corteDiarioDao = database.corteDiarioDao(),
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

    private val monitorConexion: MonitorConexion by lazy {
        MonitorConexion(applicationContext)
    }

    private val sincronizacionRepository: SincronizacionRepositoryImpl by lazy {
        SincronizacionRepositoryImpl(
            registroArchivoSyncDao = database.registroArchivoSyncDao(),
            corteDiarioDao = database.corteDiarioDao(),
            dispositivoDao = database.dispositivoDao(),
            driveServiceProvider = driveServiceProvider
        )
    }

    private val verificarConexionUseCase: VerificarConexionUseCase by lazy {
        VerificarConexionUseCase(
            monitorConexion = monitorConexion
        )
    }

    private val reintentarSincronizacionUseCase: ReintentarSincronizacionUseCase by lazy {
        ReintentarSincronizacionUseCase(
            sincronizacionRepository = sincronizacionRepository,
            verificarConexionUseCase = verificarConexionUseCase
        )
    }

    private val inicializarAppUseCase: InicializarAppUseCase by lazy {
        InicializarAppUseCase(
            negocioRepository = negocioRepository,
            dispositivoRepository = dispositivoRepository,
            configuracionAppRepository = configuracionAppRepository
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

    private val subirArchivosCorteUseCase: SubirArchivosCorteUseCase by lazy {
        SubirArchivosCorteUseCase(
            sincronizacionRepository = sincronizacionRepository
        )
    }

    private val importarVentasJsonUseCase: ImportarVentasJsonUseCase by lazy {
        ImportarVentasJsonUseCase(
            importacionRepository = importacionRepository
        )
    }

    private val obtenerVentasHistorialUseCase: ObtenerVentasHistorialUseCase by lazy {
        ObtenerVentasHistorialUseCase(
            historialRepository = historialRepository
        )
    }

    private val obtenerCortesHistorialUseCase: ObtenerCortesHistorialUseCase by lazy {
        ObtenerCortesHistorialUseCase(
            historialRepository = historialRepository
        )
    }

    private val obtenerHistorialGrupoVentaUseCase: ObtenerHistorialGrupoVentaUseCase by lazy {
        ObtenerHistorialGrupoVentaUseCase(
            historialRepository = historialRepository
        )
    }

    private val obtenerMovimientosHistorialUseCase: ObtenerMovimientosHistorialUseCase by lazy {
        ObtenerMovimientosHistorialUseCase(
            historialRepository = historialRepository
        )
    }

    private val obtenerNegociosHistorialUseCase: ObtenerNegociosHistorialUseCase by lazy {
        ObtenerNegociosHistorialUseCase(
            negocioRepository = negocioRepository
        )
    }

    private val obtenerDispositivoActualHistorialUseCase: ObtenerDispositivoActualHistorialUseCase by lazy {
        ObtenerDispositivoActualHistorialUseCase(
            dispositivoRepository = dispositivoRepository
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
            verificarConexionUseCase = verificarConexionUseCase,
            driveConectado = {
                driveServiceProvider.estaConectado()
            },
            solicitarConexionDrive = {
                solicitarConexionDrive()
            }
        )
    }

    private val sincronizacionViewModel: SincronizacionViewModel by lazy {
        SincronizacionViewModel(
            appContext = applicationContext,
            sincronizacionRepository = sincronizacionRepository,
            reintentarSincronizacionUseCase = reintentarSincronizacionUseCase
        )
    }

    private val historialViewModel: HistorialViewModel by lazy {
        HistorialViewModel(
            obtenerVentasHistorialUseCase = obtenerVentasHistorialUseCase,
            obtenerCortesHistorialUseCase = obtenerCortesHistorialUseCase,
            obtenerHistorialGrupoVentaUseCase = obtenerHistorialGrupoVentaUseCase,
            obtenerMovimientosHistorialUseCase = obtenerMovimientosHistorialUseCase,
            obtenerNegociosHistorialUseCase = obtenerNegociosHistorialUseCase,
            obtenerDispositivoActualHistorialUseCase = obtenerDispositivoActualHistorialUseCase,
            importarVentasJsonUseCase = importarVentasJsonUseCase
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
                    corteViewModel = corteViewModel,
                    sincronizacionViewModel = sincronizacionViewModel,
                    historialViewModel = historialViewModel,
                    onImportarJsonHistorial = {
                        solicitarImportarJsonHistorial()
                    },
                    onExportarCorteHistorial = { corteId ->
                        solicitarExportarCorteHistorial(corteId)
                    }
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

    private fun solicitarImportarJsonHistorial() {
        historialViewModel.mostrarSelectorImportacion()

        importarJsonLauncher.launch(
            arrayOf(
                "application/json",
                "text/plain",
                "application/octet-stream"
            )
        )
    }

    private fun solicitarExportarCorteHistorial(
        corteId: String?
    ) {
        if (corteId.isNullOrBlank()) {
            historialViewModel.mostrarMensajeAccion(
                "Selecciona un corte antes de exportar."
            )
            return
        }

        cortePendienteExportarId = corteId

        val nombreSugerido = "ventaking_${corteId.take(8)}_ventas.json"
        exportarJsonLauncher.launch(nombreSugerido)
    }

    private suspend fun exportarCorteHistorialAUri(
        corteId: String,
        uriDestino: Uri
    ) {
        try {
            when (val resultado = exportarVentasJsonUseCase(corteId)) {
                is ResultadoExportacion.Exito -> {
                    val archivoOrigen = File(resultado.archivo.rutaLocal)

                    if (!archivoOrigen.exists()) {
                        historialViewModel.mostrarMensajeAccion(
                            "No se encontró el archivo generado."
                        )
                        return
                    }

                    contentResolver.openOutputStream(uriDestino)?.use { salida ->
                        archivoOrigen.inputStream().use { entrada ->
                            entrada.copyTo(salida)
                        }
                    }

                    historialViewModel.mostrarMensajeAccion(
                        "Corte exportado correctamente."
                    )
                }

                is ResultadoExportacion.Error -> {
                    historialViewModel.mostrarMensajeAccion(resultado.mensaje)
                }
            }
        } catch (e: Exception) {
            historialViewModel.mostrarMensajeAccion(
                e.message ?: "No se pudo exportar el corte."
            )
        } finally {
            cortePendienteExportarId = null
        }
    }
}
package com.ventaking.app.presentacion.pantallas.corte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.dominio.casos.corte.CrearCorteDiarioUseCase
import com.ventaking.app.dominio.casos.corte.ObtenerResumenCorteUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteExcelUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarCorteJsonUseCase
import com.ventaking.app.dominio.casos.exportacion.ExportarVentasJsonUseCase
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import com.ventaking.app.dominio.repositorio.ResultadoCrearCorteDiario
import com.ventaking.app.dominio.repositorio.ResultadoExportacion
import com.ventaking.app.dominio.casos.sincronizacion.SubirArchivosCorteUseCase
import com.ventaking.app.dominio.repositorio.ResultadoSubidaCorte
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
import com.ventaking.app.dominio.casos.sincronizacion.VerificarConexionUseCase

class CorteViewModel(
    private val negocioDao: NegocioDao,
    private val dispositivoRepository: DispositivoRepository,
    private val obtenerResumenCorteUseCase: ObtenerResumenCorteUseCase,
    private val crearCorteDiarioUseCase: CrearCorteDiarioUseCase,
    private val exportarVentasJsonUseCase: ExportarVentasJsonUseCase,
    private val exportarCorteJsonUseCase: ExportarCorteJsonUseCase,
    private val exportarCorteExcelUseCase: ExportarCorteExcelUseCase,
    private val subirArchivosCorteUseCase: SubirArchivosCorteUseCase,
    private val verificarConexionUseCase: VerificarConexionUseCase,
    private val driveConectado: () -> Boolean,
    private val solicitarConexionDrive: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CorteUiState(
            fechaActual = obtenerFechaActual()
        )
    )
    val uiState: StateFlow<CorteUiState> = _uiState.asStateFlow()

    private var resumenJob: Job? = null

    init {
        cargarDispositivoYNegocios()
    }

    private fun cargarDispositivoYNegocios() {
        viewModelScope.launch {
            dispositivoRepository.crearDispositivoSiNoExiste()

            val dispositivoId = dispositivoRepository.obtenerIdDispositivoActual()

            _uiState.update { estado ->
                estado.copy(
                    dispositivoId = dispositivoId,
                    mensajeError = if (dispositivoId == null) {
                        "No se encontró el dispositivo local."
                    } else {
                        estado.mensajeError
                    }
                )
            }

            observarNegocios()
        }
    }

    private fun observarNegocios() {
        viewModelScope.launch {
            negocioDao.observarActivos().collect { negociosEntity ->
                val negocios = negociosEntity.map { negocio ->
                    NegocioCorteUi(
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
                    observarResumenCorte(negocioSeleccionado)
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
                totalCentavos = 0L,
                totalVentas = 0,
                totalPiezas = 0,
                corteExistenteId = null,
                mensajeError = null,
                mensajeExito = null,
                mostrarConfirmacionCorte = false
            )
        }

        observarResumenCorte(negocioId)
    }

    private fun observarResumenCorte(negocioId: String) {
        resumenJob?.cancel()

        val dispositivoId = _uiState.value.dispositivoId

        if (dispositivoId == null) {
            _uiState.update { estado ->
                estado.copy(
                    mensajeError = "No se encontró el dispositivo local."
                )
            }
            return
        }

        resumenJob = viewModelScope.launch {
            obtenerResumenCorteUseCase(
                negocioId = negocioId,
                fechaCorte = _uiState.value.fechaActual,
                dispositivoId = dispositivoId
            ).collect { resumen ->
                _uiState.update { estado ->
                    estado.copy(
                        totalCentavos = resumen.totalCentavos,
                        totalVentas = resumen.totalVentas,
                        totalPiezas = resumen.totalPiezas,
                        corteExistenteId = resumen.corteExistente?.id,
                        cargando = false,
                        mensajeError = when {
                            resumen.yaTieneCorte -> "Este negocio ya tiene un corte cerrado para hoy en este dispositivo."
                            else -> estado.mensajeError
                        }
                    )
                }
            }
        }
    }

    fun pedirConfirmacionCorte() {
        val estado = _uiState.value

        when {
            estado.negocioSeleccionadoId == null -> {
                _uiState.update {
                    it.copy(
                        mensajeError = "Selecciona un negocio.",
                        mensajeExito = null
                    )
                }
            }

            estado.dispositivoId == null -> {
                _uiState.update {
                    it.copy(
                        mensajeError = "No se encontró el dispositivo local.",
                        mensajeExito = null
                    )
                }
            }

            estado.corteExistenteId != null -> {
                _uiState.update {
                    it.copy(
                        mensajeError = "Este negocio ya tiene un corte cerrado para hoy.",
                        mensajeExito = null
                    )
                }
            }

            estado.totalVentas <= 0 -> {
                _uiState.update {
                    it.copy(
                        mensajeError = "No hay ventas activas para cortar.",
                        mensajeExito = null
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        mostrarConfirmacionCorte = true,
                        mensajeError = null,
                        mensajeExito = null
                    )
                }
            }
        }
    }

    fun cancelarConfirmacionCorte() {
        _uiState.update { estado ->
            estado.copy(mostrarConfirmacionCorte = false)
        }
    }

    fun confirmarCorte() {
        val estado = _uiState.value
        val negocioId = estado.negocioSeleccionadoId
        val dispositivoId = estado.dispositivoId

        if (negocioId == null || dispositivoId == null) {
            _uiState.update {
                it.copy(
                    mensajeError = "No se pudo confirmar el negocio o dispositivo.",
                    mensajeExito = null,
                    mostrarConfirmacionCorte = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    creandoCorte = true,
                    mostrarConfirmacionCorte = false,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            when (
                val resultado = crearCorteDiarioUseCase(
                    negocioId = negocioId,
                    fechaCorte = estado.fechaActual,
                    dispositivoId = dispositivoId
                )
            ) {
                is ResultadoCrearCorteDiario.Exito -> {
                    val resultadoExportacion = exportarArchivosLocales(resultado.corte.id)

                    when (resultadoExportacion) {
                        is ResultadoExportacionLocal.Exito -> {
                            if (!verificarConexionUseCase()) {
                                _uiState.update {
                                    it.copy(
                                        creandoCorte = false,
                                        corteExistenteId = resultado.corte.id,
                                        mensajeExito = "Corte creado y archivos locales generados. Quedó pendiente de respaldo.",
                                        mensajeError = null,
                                        mostrarModalSinInternet = true,
                                        mensajeModalSinInternet = "No hay internet en este momento. El corte se guardó correctamente en el teléfono y aparecerá en Sincronización para reintentarlo después."
                                    )
                                }

                                return@launch
                            }

                            if (driveConectado()) {
                                when (val resultadoDrive = subirArchivosCorteUseCase(resultado.corte.id)) {
                                    is ResultadoSubidaCorte.Exito -> {
                                        _uiState.update {
                                            it.copy(
                                                creandoCorte = false,
                                                corteExistenteId = resultado.corte.id,
                                                mensajeExito = "Corte creado correctamente. JSON, Excel y respaldo en Drive listos.",
                                                mensajeError = null
                                            )
                                        }

                                        limpiarMensajeExitoDespues()
                                    }

                                    is ResultadoSubidaCorte.Error -> {
                                        _uiState.update {
                                            it.copy(
                                                creandoCorte = false,
                                                corteExistenteId = resultado.corte.id,
                                                mensajeExito = "Corte creado y archivos locales generados. Respaldo Drive pendiente.",
                                                mensajeError = resultadoDrive.mensaje
                                            )
                                        }
                                    }
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        creandoCorte = false,
                                        corteExistenteId = resultado.corte.id,
                                        mensajeExito = "Corte creado y archivos locales generados. Autoriza Google Drive para respaldarlo.",
                                        mensajeError = null
                                    )
                                }

                                solicitarConexionDrive()
                            }
                        }

                        is ResultadoExportacionLocal.Error -> {
                            _uiState.update {
                                it.copy(
                                    creandoCorte = false,
                                    corteExistenteId = resultado.corte.id,
                                    mensajeExito = null,
                                    mensajeError = "El corte fue creado, pero falló la exportación local: ${resultadoExportacion.mensaje}"
                                )
                            }
                        }
                    }
                }

                is ResultadoCrearCorteDiario.YaExiste -> {
                    _uiState.update {
                        it.copy(
                            creandoCorte = false,
                            corteExistenteId = resultado.corte.id,
                            mensajeError = "Este negocio ya tiene un corte cerrado para hoy.",
                            mensajeExito = null
                        )
                    }
                }

                ResultadoCrearCorteDiario.SinVentas -> {
                    _uiState.update {
                        it.copy(
                            creandoCorte = false,
                            mensajeError = "No hay ventas activas para cortar.",
                            mensajeExito = null
                        )
                    }
                }

                is ResultadoCrearCorteDiario.Error -> {
                    _uiState.update {
                        it.copy(
                            creandoCorte = false,
                            mensajeError = resultado.mensaje,
                            mensajeExito = null
                        )
                    }
                }
            }
        }
    }
    fun respaldarCorteActualEnDrive() {
        val corteId = _uiState.value.corteExistenteId ?: return

        if (!verificarConexionUseCase()) {
            _uiState.update {
                it.copy(
                    creandoCorte = false,
                    mensajeExito = null,
                    mensajeError = "No hay internet. El corte sigue pendiente de respaldo.",
                    mostrarModalSinInternet = true,
                    mensajeModalSinInternet = "No hay internet en este momento. El respaldo se podrá reintentar desde la pantalla de Sincronización."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    creandoCorte = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            when (val resultadoDrive = subirArchivosCorteUseCase(corteId)) {
                is ResultadoSubidaCorte.Exito -> {
                    _uiState.update {
                        it.copy(
                            creandoCorte = false,
                            mensajeExito = "Respaldo en Google Drive completado.",
                            mensajeError = null
                        )
                    }

                    limpiarMensajeExitoDespues()
                }

                is ResultadoSubidaCorte.Error -> {
                    _uiState.update {
                        it.copy(
                            creandoCorte = false,
                            mensajeExito = null,
                            mensajeError = resultadoDrive.mensaje
                        )
                    }
                }
            }
        }
    }

    private suspend fun exportarArchivosLocales(corteId: String): ResultadoExportacionLocal {
        val resultados = listOf(
            exportarVentasJsonUseCase(corteId),
            exportarCorteJsonUseCase(corteId),
            exportarCorteExcelUseCase(corteId)
        )

        val errores = resultados.filterIsInstance<ResultadoExportacion.Error>()

        return if (errores.isEmpty()) {
            ResultadoExportacionLocal.Exito
        } else {
            ResultadoExportacionLocal.Error(
                mensaje = errores.joinToString(separator = " | ") { it.mensaje }
            )
        }
    }

    private fun limpiarMensajeExitoDespues() {
        viewModelScope.launch {
            delay(2500)
            _uiState.update { estado ->
                estado.copy(mensajeExito = null)
            }
        }
    }

    fun limpiarMensajeError() {
        _uiState.update { estado ->
            estado.copy(mensajeError = null)
        }
    }

    fun formatearCentavos(centavos: Long): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(centavos / 100.0)
    }

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formato.format(Date())
    }

    private sealed class ResultadoExportacionLocal {
        data object Exito : ResultadoExportacionLocal()
        data class Error(val mensaje: String) : ResultadoExportacionLocal()
    }

    fun cerrarModalSinInternet() {
        _uiState.update { estado ->
            estado.copy(
                mostrarModalSinInternet = false,
                mensajeModalSinInternet = null
            )
        }
    }
}
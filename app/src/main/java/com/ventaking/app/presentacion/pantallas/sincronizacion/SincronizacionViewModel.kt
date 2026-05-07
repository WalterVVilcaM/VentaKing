package com.ventaking.app.presentacion.pantallas.sincronizacion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ventaking.app.dominio.casos.sincronizacion.ReintentarSincronizacionUseCase
import com.ventaking.app.dominio.casos.sincronizacion.ResultadoReintentoSincronizacion
import com.ventaking.app.dominio.repositorio.SincronizacionRepository
import com.ventaking.app.trabajos.ReintentarSincronizacionWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SincronizacionViewModel(
    private val appContext: Context,
    private val sincronizacionRepository: SincronizacionRepository,
    private val reintentarSincronizacionUseCase: ReintentarSincronizacionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SincronizacionUiState())
    val uiState: StateFlow<SincronizacionUiState> = _uiState.asStateFlow()

    private var observarJob: Job? = null

    init {
        observarCortesPendientes()
    }

    private fun observarCortesPendientes() {
        observarJob?.cancel()

        observarJob = viewModelScope.launch {
            sincronizacionRepository.observarCortesPendientes().collect { cortes ->
                _uiState.update { estado ->
                    estado.copy(
                        cargando = false,
                        cortes = cortes.map { corte ->
                            CortePendienteSyncUi(
                                id = corte.id,
                                negocioId = corte.negocioId,
                                fechaCorte = corte.fechaCorte,
                                dispositivoId = corte.dispositivoId,
                                totalCentavos = corte.totalCentavos,
                                totalVentas = corte.totalVentas,
                                totalPiezas = corte.totalPiezas,
                                syncEstado = corte.syncEstado,
                                mensajeError = corte.mensajeError
                            )
                        }
                    )
                }
            }
        }
    }

    fun reintentarCorte(corteId: String) {
        if (_uiState.value.reintentandoCorteId != null || _uiState.value.reintentandoTodos) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reintentandoCorteId = corteId,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            when (val resultado = reintentarSincronizacionUseCase(corteId)) {
                is ResultadoReintentoSincronizacion.Exito -> {
                    _uiState.update {
                        it.copy(
                            reintentandoCorteId = null,
                            mensajeExito = crearMensajeExito(
                                archivosSubidos = resultado.archivosSubidos,
                                archivosOmitidos = resultado.archivosOmitidos
                            ),
                            mensajeError = null
                        )
                    }

                    limpiarMensajeExitoDespues()
                }

                ResultadoReintentoSincronizacion.SinInternet -> {
                    programarReintentoAutomatico()

                    _uiState.update {
                        it.copy(
                            reintentandoCorteId = null,
                            mensajeError = "No hay internet. El corte sigue pendiente y se reintentará cuando haya conexión.",
                            mensajeExito = null
                        )
                    }
                }

                is ResultadoReintentoSincronizacion.Error -> {
                    _uiState.update {
                        it.copy(
                            reintentandoCorteId = null,
                            mensajeError = resultado.mensaje,
                            mensajeExito = null
                        )
                    }
                }
            }
        }
    }

    fun reintentarTodos() {
        val cortes = _uiState.value.cortes
        if (cortes.isEmpty()) return
        if (_uiState.value.reintentandoCorteId != null || _uiState.value.reintentandoTodos) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reintentandoTodos = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            var exitosos = 0
            val errores = mutableListOf<String>()
            var sinInternet = false

            cortes.forEach { corte ->
                when (val resultado = reintentarSincronizacionUseCase(corte.id)) {
                    is ResultadoReintentoSincronizacion.Exito -> {
                        exitosos++
                    }

                    ResultadoReintentoSincronizacion.SinInternet -> {
                        sinInternet = true
                        return@forEach
                    }

                    is ResultadoReintentoSincronizacion.Error -> {
                        errores.add(resultado.mensaje)
                    }
                }
            }

            if (sinInternet) {
                programarReintentoAutomatico()

                _uiState.update {
                    it.copy(
                        reintentandoTodos = false,
                        mensajeError = "No hay internet. Los cortes pendientes se reintentarán cuando haya conexión.",
                        mensajeExito = null
                    )
                }

                return@launch
            }

            _uiState.update {
                it.copy(
                    reintentandoTodos = false,
                    mensajeExito = if (exitosos > 0) {
                        "Se respaldaron $exitosos corte(s) correctamente."
                    } else {
                        null
                    },
                    mensajeError = if (errores.isNotEmpty()) {
                        errores.distinct().joinToString(separator = " | ")
                    } else {
                        null
                    }
                )
            }

            if (exitosos > 0) {
                limpiarMensajeExitoDespues()
            }
        }
    }

    private fun programarReintentoAutomatico() {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<ReintentarSincronizacionWorker>()
            .setConstraints(restricciones)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            NOMBRE_TRABAJO_REINTENTO,
            ExistingWorkPolicy.KEEP,
            solicitud
        )
    }

    fun limpiarMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun formatearCentavos(centavos: Long): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(centavos / 100.0)
    }

    private fun crearMensajeExito(
        archivosSubidos: Int,
        archivosOmitidos: Int
    ): String {
        return when {
            archivosSubidos > 0 && archivosOmitidos > 0 ->
                "Respaldo completado. $archivosSubidos archivo(s) subido(s) y $archivosOmitidos ya existían."

            archivosSubidos > 0 ->
                "Respaldo completado. $archivosSubidos archivo(s) subido(s)."

            else ->
                "Respaldo verificado. Los archivos ya estaban en Drive."
        }
    }

    private fun limpiarMensajeExitoDespues() {
        viewModelScope.launch {
            delay(2500)
            _uiState.update { it.copy(mensajeExito = null) }
        }
    }

    private companion object {
        const val NOMBRE_TRABAJO_REINTENTO = "reintentar_sincronizacion_ventaking"
    }
}
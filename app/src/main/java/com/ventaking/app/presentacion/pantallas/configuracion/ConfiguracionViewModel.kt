package com.ventaking.app.presentacion.pantallas.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ventaking.app.dominio.repositorio.ConfiguracionAppRepository
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfiguracionViewModel(
    private val dispositivoRepository: DispositivoRepository,
    private val configuracionAppRepository: ConfiguracionAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        cargarConfiguracion()
    }

    fun cargarConfiguracion() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    estaCargando = true,
                    error = null,
                    mensaje = null
                )

                val dispositivo = dispositivoRepository.obtenerDispositivoActual()
                val configuracion = configuracionAppRepository.obtenerConfiguracionActual()

                _uiState.value = _uiState.value.copy(
                    estaCargando = false,
                    dispositivoId = dispositivo?.id.orEmpty(),
                    nombreDispositivo = dispositivo?.nombreDispositivo.orEmpty(),
                    nombreDispositivoEditable = dispositivo?.nombreDispositivo.orEmpty(),
                    tema = configuracion?.tema ?: "SISTEMA",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    estaCargando = false,
                    error = "No se pudo cargar la configuración."
                )
            }
        }
    }

    fun actualizarCampoNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(
            nombreDispositivoEditable = nombre,
            mensaje = null,
            error = null
        )
    }

    fun guardarNombreDispositivo() {
        viewModelScope.launch {
            try {
                val nombreLimpio = _uiState.value.nombreDispositivoEditable.trim()

                if (nombreLimpio.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        error = "El nombre del dispositivo no puede estar vacío.",
                        mensaje = null
                    )
                    return@launch
                }

                dispositivoRepository.actualizarNombreDispositivo(nombreLimpio)

                _uiState.value = _uiState.value.copy(
                    nombreDispositivo = nombreLimpio,
                    nombreDispositivoEditable = nombreLimpio,
                    mensaje = "Nombre del dispositivo actualizado.",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo guardar el nombre del dispositivo.",
                    mensaje = null
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            mensaje = null,
            error = null
        )
    }
}
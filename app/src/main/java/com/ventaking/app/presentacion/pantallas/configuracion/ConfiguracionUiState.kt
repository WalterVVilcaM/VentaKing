package com.ventaking.app.presentacion.pantallas.configuracion

data class ConfiguracionUiState(
    val estaCargando: Boolean = true,
    val dispositivoId: String = "",
    val nombreDispositivo: String = "",
    val nombreDispositivoEditable: String = "",
    val tema: String = "SISTEMA",
    val mensaje: String? = null,
    val error: String? = null
)
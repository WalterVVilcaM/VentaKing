package com.ventaking.app.dominio.modelo

data class Dispositivo(
    val id: String,
    val nombreDispositivo: String?,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val ultimoRespaldoEn: Long?
) {
    val nombreVisible: String
        get() = nombreDispositivo?.takeIf { it.isNotBlank() } ?: "Dispositivo local"
}
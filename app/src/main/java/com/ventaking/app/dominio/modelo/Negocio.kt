package com.ventaking.app.dominio.modelo

data class Negocio(
    val id: String,
    val nombre: String,
    val estaActivo: Boolean,
    val creadoEn: Long,
    val actualizadoEn: Long
)
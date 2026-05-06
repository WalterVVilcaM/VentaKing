package com.ventaking.app.dominio.modelo

data class ArchivoExportado(
    val id: String,
    val corteId: String,
    val negocioId: String,
    val dispositivoId: String,
    val fechaCorte: String,
    val tipoArchivo: String,
    val nombreArchivo: String,
    val rutaLocal: String,
    val hashArchivo: String,
    val creadoEn: Long,
    val syncEstado: String
)
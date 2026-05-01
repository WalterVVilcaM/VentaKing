package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros_archivos_sync",
    indices = [
        Index(value = ["corteId"]),
        Index(value = ["negocioId"]),
        Index(value = ["dispositivoId"]),
        Index(value = ["fechaCorte"]),
        Index(value = ["nombreArchivo"]),
        Index(value = ["hashArchivo"]),
        Index(value = ["syncEstado"])
    ]
)
data class RegistroArchivoSyncEntity(
    @PrimaryKey val id: String,
    val corteId: String,
    val negocioId: String,
    val dispositivoId: String,
    val fechaCorte: String,
    val tipoArchivo: String,
    val nombreArchivo: String,
    val rutaLocal: String,
    val hashArchivo: String,
    val driveFileId: String? = null,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val sincronizadoEn: Long? = null,
    val syncEstado: String,
    val mensajeError: String? = null
)
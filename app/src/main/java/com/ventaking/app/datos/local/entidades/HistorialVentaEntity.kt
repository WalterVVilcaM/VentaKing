package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historial_ventas",
    indices = [
        Index(value = ["ventaId"]),
        Index(value = ["grupoVentaId"]),
        Index(value = ["negocioId"]),
        Index(value = ["dispositivoId"]),
        Index(value = ["tipoAccion"]),
        Index(value = ["syncEstado"])
    ]
)
data class HistorialVentaEntity(
    @PrimaryKey val id: String,
    val ventaId: String,
    val grupoVentaId: String,
    val negocioId: String,
    val dispositivoId: String,
    val tipoAccion: String,
    val totalAnteriorCentavos: Long?,
    val totalNuevoCentavos: Long?,
    val cantidadAnterior: Int?,
    val cantidadNueva: Int?,
    val nota: String?,
    val creadoEn: Long,
    val syncEstado: String
)
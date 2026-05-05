package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ventas",
    indices = [
        Index(value = ["grupoVentaId"]),
        Index(value = ["negocioId"]),
        Index(value = ["productoId"]),
        Index(value = ["dispositivoId"]),
        Index(value = ["corteId"]),
        Index(value = ["fechaVenta"]),
        Index(value = ["estado"]),
        Index(value = ["syncEstado"])
    ]
)
data class VentaEntity(
    @PrimaryKey val id: String,
    val grupoVentaId: String,
    val negocioId: String,
    val productoId: String?,
    val nombreProductoSnapshot: String,
    val cantidad: Int,
    val precioUnitarioSnapshotCentavos: Long,
    val subtotalCentavos: Long,
    val extraCentavos: Long = 0L,
    val descuentoCentavos: Long = 0L,
    val totalCentavos: Long,
    val fechaVenta: String,
    val horaVenta: String,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val dispositivoId: String,
    val corteId: String? = null,
    val estado: String,
    val syncEstado: String
)
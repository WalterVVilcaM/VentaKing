package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cortes_diarios",
    indices = [
        Index(value = ["negocioId"]),
        Index(value = ["fechaCorte"]),
        Index(value = ["dispositivoId"]),
        Index(value = ["estado"]),
        Index(value = ["syncEstado"])
    ]
)
data class CorteDiarioEntity(
    @PrimaryKey val id: String,
    val negocioId: String,
    val fechaCorte: String,
    val dispositivoId: String,
    val totalCentavos: Long,
    val totalVentas: Int,
    val totalPiezas: Int,
    val creadoEn: Long,
    val cerradoEn: Long,
    val estado: String,
    val syncEstado: String,
    val sincronizadoEn: Long? = null,
    val mensajeError: String? = null
)
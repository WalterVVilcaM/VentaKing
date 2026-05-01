package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos_rapidos",
    indices = [
        Index(value = ["negocioId"]),
        Index(value = ["nombre"])
    ]
)
data class ProductoRapidoEntity(
    @PrimaryKey val id: String,
    val negocioId: String,
    val nombre: String,
    val precioCentavos: Long,
    val categoria: String? = null,
    val esPromocion: Boolean = false,
    val estaActivo: Boolean = true,
    val creadoEn: Long,
    val actualizadoEn: Long
)
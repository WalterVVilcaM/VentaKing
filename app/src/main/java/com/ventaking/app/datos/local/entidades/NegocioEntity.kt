package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "negocios")
data class NegocioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val estaActivo: Boolean = true,
    val creadoEn: Long,
    val actualizadoEn: Long
)
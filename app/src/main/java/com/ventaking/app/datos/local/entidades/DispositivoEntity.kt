package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispositivo")
data class DispositivoEntity(
    @PrimaryKey val id: String,
    val nombreDispositivo: String?,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val ultimoRespaldoEn: Long?
)
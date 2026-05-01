package com.ventaking.app.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_app")
data class ConfiguracionAppEntity(
    @PrimaryKey val id: String,
    val tema: String,
    val carpetaDriveId: String? = null,
    val carpetaDriveNombre: String? = null,
    val creadoEn: Long,
    val actualizadoEn: Long
)
package com.ventaking.app.datos.local.base

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ventaking.app.datos.local.dao.ConfiguracionAppDao
import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.DispositivoDao
import com.ventaking.app.datos.local.dao.HistorialVentaDao
import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.datos.local.dao.ProductoRapidoDao
import com.ventaking.app.datos.local.dao.RegistroArchivoSyncDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.ConfiguracionAppEntity
import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.datos.local.entidades.ProductoRapidoEntity
import com.ventaking.app.datos.local.entidades.RegistroArchivoSyncEntity
import com.ventaking.app.datos.local.entidades.VentaEntity

@Database(
    entities = [
        NegocioEntity::class,
        ProductoRapidoEntity::class,
        VentaEntity::class,
        HistorialVentaEntity::class,
        CorteDiarioEntity::class,
        DispositivoEntity::class,
        RegistroArchivoSyncEntity::class,
        ConfiguracionAppEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun negocioDao(): NegocioDao

    abstract fun productoRapidoDao(): ProductoRapidoDao

    abstract fun ventaDao(): VentaDao

    abstract fun historialVentaDao(): HistorialVentaDao

    abstract fun corteDiarioDao(): CorteDiarioDao

    abstract fun dispositivoDao(): DispositivoDao

    abstract fun registroArchivoSyncDao(): RegistroArchivoSyncDao

    abstract fun configuracionAppDao(): ConfiguracionAppDao
}
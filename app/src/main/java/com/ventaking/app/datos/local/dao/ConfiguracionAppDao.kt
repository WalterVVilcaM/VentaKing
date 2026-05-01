package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.ConfiguracionAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(configuracion: ConfiguracionAppEntity)

    @Update
    suspend fun actualizar(configuracion: ConfiguracionAppEntity)

    @Query("SELECT * FROM configuracion_app LIMIT 1")
    suspend fun obtenerActual(): ConfiguracionAppEntity?

    @Query("SELECT * FROM configuracion_app LIMIT 1")
    fun observarActual(): Flow<ConfiguracionAppEntity?>

    @Query("""
        UPDATE configuracion_app
        SET tema = :tema,
            actualizadoEn = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun actualizarTema(
        id: String,
        tema: String,
        actualizadoEn: Long
    )

    @Query("""
        UPDATE configuracion_app
        SET carpetaDriveId = :carpetaDriveId,
            carpetaDriveNombre = :carpetaDriveNombre,
            actualizadoEn = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun actualizarCarpetaDrive(
        id: String,
        carpetaDriveId: String?,
        carpetaDriveNombre: String?,
        actualizadoEn: Long
    )
}
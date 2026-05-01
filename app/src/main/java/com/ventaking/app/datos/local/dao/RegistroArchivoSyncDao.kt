package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.RegistroArchivoSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroArchivoSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: RegistroArchivoSyncEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(registros: List<RegistroArchivoSyncEntity>)

    @Update
    suspend fun actualizar(registro: RegistroArchivoSyncEntity)

    @Query("SELECT * FROM registros_archivos_sync WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): RegistroArchivoSyncEntity?

    @Query("""
        SELECT * FROM registros_archivos_sync
        WHERE corteId = :corteId
        ORDER BY creadoEn ASC
    """)
    suspend fun obtenerPorCorte(corteId: String): List<RegistroArchivoSyncEntity>

    @Query("""
        SELECT * FROM registros_archivos_sync
        WHERE syncEstado IN (:estados)
        ORDER BY creadoEn ASC
    """)
    fun observarPorEstadosSync(estados: List<String>): Flow<List<RegistroArchivoSyncEntity>>

    @Query("""
        SELECT * FROM registros_archivos_sync
        WHERE hashArchivo = :hashArchivo
        LIMIT 1
    """)
    suspend fun obtenerPorHash(hashArchivo: String): RegistroArchivoSyncEntity?

    @Query("""
        UPDATE registros_archivos_sync
        SET driveFileId = :driveFileId,
            syncEstado = :syncEstado,
            sincronizadoEn = :sincronizadoEn,
            actualizadoEn = :actualizadoEn,
            mensajeError = :mensajeError
        WHERE id = :id
    """)
    suspend fun actualizarResultadoSync(
        id: String,
        driveFileId: String?,
        syncEstado: String,
        sincronizadoEn: Long?,
        actualizadoEn: Long,
        mensajeError: String?
    )
}
package com.ventaking.app.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ventaking.app.datos.local.entidades.NegocioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NegocioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(negocio: NegocioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(negocios: List<NegocioEntity>)

    @Update
    suspend fun actualizar(negocio: NegocioEntity)

    @Query("SELECT * FROM negocios ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<NegocioEntity>>

    @Query("SELECT * FROM negocios WHERE estaActivo = 1 ORDER BY nombre ASC")
    fun observarActivos(): Flow<List<NegocioEntity>>

    @Query("SELECT * FROM negocios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): NegocioEntity?

    @Query("SELECT * FROM negocios WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): NegocioEntity?

    @Query("SELECT COUNT(*) FROM negocios")
    suspend fun contar(): Int
}
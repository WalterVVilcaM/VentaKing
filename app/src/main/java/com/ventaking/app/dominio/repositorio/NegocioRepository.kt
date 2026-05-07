package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.Negocio
import kotlinx.coroutines.flow.Flow

interface NegocioRepository {

    suspend fun contarNegocios(): Int

    suspend fun insertarNegociosBaseSiNoExisten()

    fun observarNegociosActivos(): Flow<List<Negocio>>
}
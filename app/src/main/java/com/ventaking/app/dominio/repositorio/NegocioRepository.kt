package com.ventaking.app.dominio.repositorio

interface NegocioRepository {

    suspend fun contarNegocios(): Int

    suspend fun insertarNegociosBaseSiNoExisten()
}
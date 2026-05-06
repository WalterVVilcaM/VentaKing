package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.Venta
import kotlinx.coroutines.flow.Flow

interface CorteRepository {

    fun observarVentasAbiertasParaCorte(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): Flow<List<Venta>>

    suspend fun obtenerCorteExistente(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): CorteDiario?

    suspend fun crearCorteDiario(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): ResultadoCrearCorteDiario
}

sealed class ResultadoCrearCorteDiario {
    data class Exito(val corte: CorteDiario) : ResultadoCrearCorteDiario()
    data class YaExiste(val corte: CorteDiario) : ResultadoCrearCorteDiario()
    data object SinVentas : ResultadoCrearCorteDiario()
    data class Error(val mensaje: String) : ResultadoCrearCorteDiario()
}
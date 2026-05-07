package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Venta
import kotlinx.coroutines.flow.Flow

interface HistorialRepository {

    fun observarVentasHistorial(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String?,
        estado: String?
    ): Flow<List<Venta>>

    fun observarCortesHistorial(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String?
    ): Flow<List<CorteDiario>>

    fun observarHistorialDeVenta(
        ventaId: String
    ): Flow<List<HistorialVenta>>

    fun observarHistorialDeGrupoVenta(
        grupoVentaId: String
    ): Flow<List<HistorialVenta>>

    fun observarMovimientosHistorial(
        negocioId: String,
        dispositivoId: String?,
        desde: Long,
        hasta: Long
    ): Flow<List<HistorialVenta>>
}
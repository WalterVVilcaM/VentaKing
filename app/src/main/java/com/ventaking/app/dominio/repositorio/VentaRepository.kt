package com.ventaking.app.dominio.repositorio

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Venta
import kotlinx.coroutines.flow.Flow

interface VentaRepository {

    suspend fun registrarVenta(venta: Venta)

    suspend fun registrarHistorialVenta(historialVenta: HistorialVenta)

    fun observarVentasPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<Venta>>

    fun observarVentasActivasPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<Venta>>

    fun observarTotalActivoPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<Long?>
}
package com.ventaking.app.dominio.casos.ventas

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.repositorio.VentaRepository
import com.ventaking.app.nucleo.constantes.HistorialAccion
import com.ventaking.app.nucleo.constantes.SyncEstado
import com.ventaking.app.nucleo.constantes.VentaEstado
import java.util.UUID

class CancelarVentaUseCase(
    private val ventaRepository: VentaRepository
) {

    suspend operator fun invoke(
        ventaId: String,
        nota: String? = null
    ): ResultadoCancelarVenta {
        val ventaAnterior = ventaRepository.obtenerVentaPorId(ventaId)
            ?: return ResultadoCancelarVenta.Error("No se encontró la venta.")

        if (ventaAnterior.corteId != null) {
            return ResultadoCancelarVenta.Error("No se puede cancelar una venta que ya pertenece a un corte.")
        }

        if (ventaAnterior.estado == VentaEstado.CLOSED) {
            return ResultadoCancelarVenta.Error("No se puede cancelar una venta cerrada.")
        }

        if (ventaAnterior.estado == VentaEstado.CANCELLED) {
            return ResultadoCancelarVenta.Error("Esta venta ya está cancelada.")
        }

        if (ventaAnterior.estado != VentaEstado.ACTIVE) {
            return ResultadoCancelarVenta.Error("Solo se pueden cancelar ventas activas.")
        }

        val ahora = System.currentTimeMillis()

        val ventaCancelada = ventaAnterior.copy(
            estado = VentaEstado.CANCELLED,
            actualizadoEn = ahora,
            syncEstado = SyncEstado.LOCAL_ONLY
        )

        ventaRepository.actualizarVenta(ventaCancelada)

        ventaRepository.registrarHistorialVenta(
            HistorialVenta(
                id = UUID.randomUUID().toString(),
                ventaId = ventaAnterior.id,
                grupoVentaId = ventaAnterior.grupoVentaId,
                negocioId = ventaAnterior.negocioId,
                dispositivoId = ventaAnterior.dispositivoId,
                tipoAccion = HistorialAccion.CANCELLED,
                totalAnteriorCentavos = ventaAnterior.totalCentavos,
                totalNuevoCentavos = 0L,
                cantidadAnterior = ventaAnterior.cantidad,
                cantidadNueva = 0,
                nota = nota?.takeIf { it.isNotBlank() } ?: "Venta cancelada antes del corte.",
                creadoEn = ahora,
                syncEstado = SyncEstado.LOCAL_ONLY
            )
        )

        return ResultadoCancelarVenta.Exito
    }
}

sealed class ResultadoCancelarVenta {
    data object Exito : ResultadoCancelarVenta()

    data class Error(
        val mensaje: String
    ) : ResultadoCancelarVenta()
}
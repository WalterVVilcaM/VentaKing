package com.ventaking.app.dominio.casos.ventas

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.repositorio.VentaRepository
import com.ventaking.app.nucleo.constantes.HistorialAccion
import com.ventaking.app.nucleo.constantes.SyncEstado
import com.ventaking.app.nucleo.constantes.VentaEstado
import java.util.UUID

class EditarVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val calcularTotalVentaUseCase: CalcularTotalVentaUseCase
) {

    suspend operator fun invoke(
        ventaId: String,
        nuevaCantidad: Int,
        nuevoExtraCentavos: Long,
        nuevoDescuentoCentavos: Long,
        nota: String? = null
    ): ResultadoEditarVenta {
        val ventaAnterior = ventaRepository.obtenerVentaPorId(ventaId)
            ?: return ResultadoEditarVenta.Error("No se encontró la venta.")

        if (ventaAnterior.corteId != null) {
            return ResultadoEditarVenta.Error("No se puede editar una venta que ya pertenece a un corte.")
        }

        if (ventaAnterior.estado == VentaEstado.CLOSED) {
            return ResultadoEditarVenta.Error("No se puede editar una venta cerrada.")
        }

        if (ventaAnterior.estado != VentaEstado.ACTIVE) {
            return ResultadoEditarVenta.Error("Solo se pueden editar ventas activas.")
        }

        val resultadoCalculo = calcularTotalVentaUseCase(
            precioUnitarioCentavos = ventaAnterior.precioUnitarioSnapshotCentavos,
            cantidad = nuevaCantidad,
            extraCentavos = nuevoExtraCentavos,
            descuentoCentavos = nuevoDescuentoCentavos
        )

        when (resultadoCalculo) {
            is ResultadoCalculoVenta.Error -> {
                return ResultadoEditarVenta.Error(resultadoCalculo.mensaje)
            }

            is ResultadoCalculoVenta.Exito -> {
                val ahora = System.currentTimeMillis()

                val ventaActualizada = ventaAnterior.copy(
                    cantidad = nuevaCantidad,
                    subtotalCentavos = resultadoCalculo.subtotalCentavos,
                    extraCentavos = nuevoExtraCentavos,
                    descuentoCentavos = nuevoDescuentoCentavos,
                    totalCentavos = resultadoCalculo.totalCentavos,
                    actualizadoEn = ahora,
                    syncEstado = SyncEstado.LOCAL_ONLY
                )

                ventaRepository.actualizarVenta(ventaActualizada)

                ventaRepository.registrarHistorialVenta(
                    HistorialVenta(
                        id = UUID.randomUUID().toString(),
                        ventaId = ventaAnterior.id,
                        grupoVentaId = ventaAnterior.grupoVentaId,
                        negocioId = ventaAnterior.negocioId,
                        dispositivoId = ventaAnterior.dispositivoId,
                        tipoAccion = HistorialAccion.UPDATED,
                        totalAnteriorCentavos = ventaAnterior.totalCentavos,
                        totalNuevoCentavos = ventaActualizada.totalCentavos,
                        cantidadAnterior = ventaAnterior.cantidad,
                        cantidadNueva = ventaActualizada.cantidad,
                        nota = nota?.takeIf { it.isNotBlank() },
                        creadoEn = ahora,
                        syncEstado = SyncEstado.LOCAL_ONLY
                    )
                )

                return ResultadoEditarVenta.Exito
            }
        }
    }
}

sealed class ResultadoEditarVenta {
    data object Exito : ResultadoEditarVenta()

    data class Error(
        val mensaje: String
    ) : ResultadoEditarVenta()
}
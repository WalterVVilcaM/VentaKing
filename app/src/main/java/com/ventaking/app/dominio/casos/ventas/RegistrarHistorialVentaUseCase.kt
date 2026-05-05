package com.ventaking.app.dominio.casos.ventas

import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.VentaRepository
import com.ventaking.app.nucleo.constantes.HistorialAccion
import com.ventaking.app.nucleo.constantes.SyncEstado
import java.util.UUID

class RegistrarHistorialVentaUseCase(
    private val ventaRepository: VentaRepository
) {

    suspend operator fun invoke(
        venta: Venta,
        nota: String? = "Venta registrada localmente"
    ) {
        val ahora = System.currentTimeMillis()

        val historial = HistorialVenta(
            id = UUID.randomUUID().toString(),
            ventaId = venta.id,
            grupoVentaId = venta.grupoVentaId,
            negocioId = venta.negocioId,
            dispositivoId = venta.dispositivoId,
            tipoAccion = HistorialAccion.CREATED,
            totalAnteriorCentavos = null,
            totalNuevoCentavos = venta.totalCentavos,
            cantidadAnterior = null,
            cantidadNueva = venta.cantidad,
            nota = nota,
            creadoEn = ahora,
            syncEstado = SyncEstado.LOCAL_ONLY
        )

        ventaRepository.registrarHistorialVenta(historial)
    }
}
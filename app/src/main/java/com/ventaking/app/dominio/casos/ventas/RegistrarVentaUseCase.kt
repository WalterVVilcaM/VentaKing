package com.ventaking.app.dominio.casos.ventas

import com.ventaking.app.dominio.modelo.ProductoRapido
import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.DispositivoRepository
import com.ventaking.app.dominio.repositorio.VentaRepository
import com.ventaking.app.nucleo.constantes.SyncEstado
import com.ventaking.app.nucleo.constantes.VentaEstado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RegistrarVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val dispositivoRepository: DispositivoRepository,
    private val calcularTotalVentaUseCase: CalcularTotalVentaUseCase,
    private val registrarHistorialVentaUseCase: RegistrarHistorialVentaUseCase
) {

    suspend operator fun invoke(
        grupoVentaId: String,
        negocioId: String,
        producto: ProductoRapido,
        cantidad: Int,
        extraCentavos: Long,
        descuentoCentavos: Long
    ): ResultadoRegistrarVenta {
        if (grupoVentaId.isBlank()) {
            return ResultadoRegistrarVenta.Error("No se pudo crear el grupo de venta.")
        }

        if (negocioId.isBlank()) {
            return ResultadoRegistrarVenta.Error("Selecciona un negocio antes de vender.")
        }

        if (!producto.estaActivo) {
            return ResultadoRegistrarVenta.Error("El producto seleccionado está inactivo.")
        }

        val dispositivoId = dispositivoRepository.obtenerIdDispositivoActual()
            ?: return ResultadoRegistrarVenta.Error("No se encontró el dispositivo local. Reinicia la app para inicializarlo.")

        val resultadoCalculo = calcularTotalVentaUseCase(
            precioUnitarioCentavos = producto.precioCentavos,
            cantidad = cantidad,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos
        )

        if (resultadoCalculo is ResultadoCalculoVenta.Error) {
            return ResultadoRegistrarVenta.Error(resultadoCalculo.mensaje)
        }

        resultadoCalculo as ResultadoCalculoVenta.Exito

        val ahora = System.currentTimeMillis()

        val venta = Venta(
            id = UUID.randomUUID().toString(),
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            productoId = producto.id,
            nombreProductoSnapshot = producto.nombre,
            cantidad = cantidad,
            precioUnitarioSnapshotCentavos = producto.precioCentavos,
            subtotalCentavos = resultadoCalculo.subtotalCentavos,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos,
            totalCentavos = resultadoCalculo.totalCentavos,
            fechaVenta = obtenerFechaActual(),
            horaVenta = obtenerHoraActual(),
            creadoEn = ahora,
            actualizadoEn = ahora,
            dispositivoId = dispositivoId,
            corteId = null,
            estado = VentaEstado.ACTIVE,
            syncEstado = SyncEstado.LOCAL_ONLY
        )

        ventaRepository.registrarVenta(venta)
        registrarHistorialVentaUseCase(venta)

        return ResultadoRegistrarVenta.Exito(venta)
    }

    private fun obtenerFechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun obtenerHoraActual(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
}

sealed class ResultadoRegistrarVenta {
    data class Exito(
        val venta: Venta
    ) : ResultadoRegistrarVenta()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistrarVenta()
}
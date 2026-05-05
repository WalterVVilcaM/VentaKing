package com.ventaking.app.dominio.casos.ventas

class CalcularTotalVentaUseCase {

    operator fun invoke(
        precioUnitarioCentavos: Long,
        cantidad: Int,
        extraCentavos: Long,
        descuentoCentavos: Long
    ): ResultadoCalculoVenta {
        if (precioUnitarioCentavos <= 0L) {
            return ResultadoCalculoVenta.Error("El precio del producto debe ser mayor a cero.")
        }

        if (cantidad < 1) {
            return ResultadoCalculoVenta.Error("La cantidad no puede ser menor a 1.")
        }

        if (extraCentavos < 0L) {
            return ResultadoCalculoVenta.Error("El extra no puede ser negativo.")
        }

        if (descuentoCentavos < 0L) {
            return ResultadoCalculoVenta.Error("El descuento no puede ser negativo.")
        }

        val subtotalCentavos = precioUnitarioCentavos * cantidad
        val totalAntesDescuento = subtotalCentavos + extraCentavos

        if (descuentoCentavos >= totalAntesDescuento) {
            return ResultadoCalculoVenta.Error("El descuento no puede ser igual o mayor al subtotal más extra.")
        }

        val totalCentavos = totalAntesDescuento - descuentoCentavos

        if (totalCentavos <= 0L) {
            return ResultadoCalculoVenta.Error("El total de la venta debe ser mayor a cero.")
        }

        return ResultadoCalculoVenta.Exito(
            subtotalCentavos = subtotalCentavos,
            totalCentavos = totalCentavos
        )
    }
}

sealed class ResultadoCalculoVenta {
    data class Exito(
        val subtotalCentavos: Long,
        val totalCentavos: Long
    ) : ResultadoCalculoVenta()

    data class Error(
        val mensaje: String
    ) : ResultadoCalculoVenta()
}
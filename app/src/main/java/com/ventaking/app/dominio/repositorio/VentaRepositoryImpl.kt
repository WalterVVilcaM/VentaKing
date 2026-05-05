package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.local.dao.HistorialVentaDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.VentaRepository
import com.ventaking.app.nucleo.constantes.VentaEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VentaRepositoryImpl(
    private val ventaDao: VentaDao,
    private val historialVentaDao: HistorialVentaDao
) : VentaRepository {

    override suspend fun registrarVenta(venta: Venta) {
        ventaDao.insertar(venta.toEntity())
    }

    override suspend fun registrarHistorialVenta(historialVenta: HistorialVenta) {
        historialVentaDao.insertar(historialVenta.toEntity())
    }

    override suspend fun obtenerVentaPorId(ventaId: String): Venta? {
        return ventaDao.obtenerPorId(ventaId)?.toDomain()
    }

    override suspend fun actualizarVenta(venta: Venta) {
        ventaDao.actualizar(venta.toEntity())
    }

    override fun observarVentasPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<Venta>> {
        return ventaDao
            .observarPorNegocioYFecha(
                negocioId = negocioId,
                fechaVenta = fechaVenta
            )
            .map { ventas ->
                ventas.map { it.toDomain() }
            }
    }

    override fun observarVentasActivasPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<List<Venta>> {
        return ventaDao
            .observarPorNegocioFechaYEstado(
                negocioId = negocioId,
                fechaVenta = fechaVenta,
                estado = VentaEstado.ACTIVE
            )
            .map { ventas ->
                ventas.map { it.toDomain() }
            }
    }

    override fun observarTotalActivoPorNegocioYFecha(
        negocioId: String,
        fechaVenta: String
    ): Flow<Long?> {
        return ventaDao.observarTotalPorNegocioFechaYEstado(
            negocioId = negocioId,
            fechaVenta = fechaVenta,
            estado = VentaEstado.ACTIVE
        )
    }

    private fun VentaEntity.toDomain(): Venta {
        return Venta(
            id = id,
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            productoId = productoId,
            nombreProductoSnapshot = nombreProductoSnapshot,
            cantidad = cantidad,
            precioUnitarioSnapshotCentavos = precioUnitarioSnapshotCentavos,
            subtotalCentavos = subtotalCentavos,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos,
            totalCentavos = totalCentavos,
            fechaVenta = fechaVenta,
            horaVenta = horaVenta,
            creadoEn = creadoEn,
            actualizadoEn = actualizadoEn,
            dispositivoId = dispositivoId,
            corteId = corteId,
            estado = estado,
            syncEstado = syncEstado
        )
    }

    private fun Venta.toEntity(): VentaEntity {
        return VentaEntity(
            id = id,
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            productoId = productoId,
            nombreProductoSnapshot = nombreProductoSnapshot,
            cantidad = cantidad,
            precioUnitarioSnapshotCentavos = precioUnitarioSnapshotCentavos,
            subtotalCentavos = subtotalCentavos,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos,
            totalCentavos = totalCentavos,
            fechaVenta = fechaVenta,
            horaVenta = horaVenta,
            creadoEn = creadoEn,
            actualizadoEn = actualizadoEn,
            dispositivoId = dispositivoId,
            corteId = corteId,
            estado = estado,
            syncEstado = syncEstado
        )
    }

    private fun HistorialVenta.toEntity(): HistorialVentaEntity {
        return HistorialVentaEntity(
            id = id,
            ventaId = ventaId,
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            dispositivoId = dispositivoId,
            tipoAccion = tipoAccion,
            totalAnteriorCentavos = totalAnteriorCentavos,
            totalNuevoCentavos = totalNuevoCentavos,
            cantidadAnterior = cantidadAnterior,
            cantidadNueva = cantidadNueva,
            nota = nota,
            creadoEn = creadoEn,
            syncEstado = syncEstado
        )
    }
}